// 声明下单核心业务所在的包。
package com.liushuai.ticket.order;

// 导入活动数据库仓储，用于最终扣减持久库存。
import com.liushuai.ticket.activity.ActivityRepository;
// 导入活动服务，用于校验活动存在并读取库存。
import com.liushuai.ticket.activity.ActivityService;
// 导入可安全返回给前端的业务异常。
import com.liushuai.ticket.common.BusinessException;
// 导入 Redis 脚本键列表使用的集合类型。
import java.util.List;
// 导入 Spring 应用事件发布器。
import org.springframework.context.ApplicationEventPublisher;
// 导入数据库唯一约束等冲突异常。
import org.springframework.dao.DataIntegrityViolationException;
// 导入 Redis 字符串模板。
import org.springframework.data.redis.core.StringRedisTemplate;
// 导入 Lua 脚本封装类型。
import org.springframework.data.redis.core.script.DefaultRedisScript;
// 导入服务层注解。
import org.springframework.stereotype.Service;
// 导入声明式事务注解。
import org.springframework.transaction.annotation.Transactional;

// 注册为下单服务，协调缓存、数据库与异步事件。
@Service
public class OrderService {
    // 定义活动库存缓存键的格式。
    private static final String STOCK_KEY = "ticket:activity:%d:stock";
    // 定义请求幂等缓存键的格式。
    private static final String IDEMPOTENCY_KEY = "ticket:request:%s";
    // 保存活动服务。
    private final ActivityService activityService;
    // 保存活动仓储。
    private final ActivityRepository activityRepository;
    // 保存订单仓储。
    private final OrderRepository orderRepository;
    // 保存 Redis 操作模板。
    private final StringRedisTemplate redis;
    // 保存原子预占库存 Lua 脚本。
    private final DefaultRedisScript<Long> reserveStockScript;
    // 保存领域事件发布器。
    private final ApplicationEventPublisher eventPublisher;

    // 通过构造器注入下单所需依赖。
    public OrderService(ActivityService activityService, ActivityRepository activityRepository,
                        OrderRepository orderRepository, StringRedisTemplate redis,
                        DefaultRedisScript<Long> reserveStockScript, ApplicationEventPublisher eventPublisher) {
        // 保存活动服务。
        this.activityService = activityService;
        // 保存活动仓储。
        this.activityRepository = activityRepository;
        // 保存订单仓储。
        this.orderRepository = orderRepository;
        // 保存 Redis 模板。
        this.redis = redis;
        // 保存库存预占脚本。
        this.reserveStockScript = reserveStockScript;
        // 保存事件发布器。
        this.eventPublisher = eventPublisher;
    }

    // 在事务内完成订单创建与数据库库存扣减。
    @Transactional
    public TicketOrder create(CreateOrderRequest request) {
        // 先按请求 ID 查重，使同一请求可安全重试。
        var existingRequest = orderRepository.findByRequestId(request.requestId());
        // 已有订单时直接返回原订单，避免重复扣库存。
        if (existingRequest.isPresent()) {
            return existingRequest.get();
        }
        // 限制同一用户对同一活动只能成功购买一次。
        orderRepository.findByUserAndActivity(request.userId(), request.activityId())
                .ifPresent(order -> { throw new BusinessException(409, "同一活动每位用户只能购买一次"); });
        // 校验目标活动存在。
        activityService.get(request.activityId());

        // 生成当前活动的库存键。
        String stockKey = STOCK_KEY.formatted(request.activityId());
        // 生成本次请求的幂等键。
        String idempotencyKey = IDEMPOTENCY_KEY.formatted(request.requestId());
        // 原子检查缓存库存、扣减库存并设置短期 PENDING 标记。
        Long result = redis.execute(reserveStockScript, List.of(stockKey, idempotencyKey), String.valueOf(request.quantity()), "300");
        // -3 或空结果表示缓存缺失，先从数据库修复缓存。
        if (result == null || result == -3) {
            // 将数据库的当前库存写回 Redis。
            redis.opsForValue().set(stockKey, String.valueOf(activityService.get(request.activityId()).availableStock()));
            // 提示前端重试，避免在不确定的缓存状态下继续下单。
            throw new BusinessException(503, "库存缓存已刷新，请重试");
        }
        // 0 表示同一请求正在处理中。
        if (result == 0) {
            throw new BusinessException(409, "请求正在处理，请勿重复提交");
        }
        // -1 表示 Redis 中的可用库存不足。
        if (result == -1) {
            throw new BusinessException(409, "余票不足");
        }

        // Redis 预占成功后，尝试持久化订单和数据库库存。
        try {
            // 写入订单记录。
            orderRepository.insert(request);
            // 数据库条件更新失败时表示最终库存校验不通过。
            if (activityRepository.decreaseDbStock(request.activityId(), request.quantity()) != 1) {
                // 交给 catch 分支补偿 Redis 库存。
                throw new BusinessException(409, "余票不足");
            }
            // 读取刚插入的完整订单（含数据库生成的 ID 和时间）。
            TicketOrder order = orderRepository.findByRequestId(request.requestId()).orElseThrow();
            // 将 PENDING 幂等标记改为真实订单 ID。
            redis.opsForValue().set(idempotencyKey, String.valueOf(order.id()));
            // 发布订单已创建事件，事务提交后会异步发送 Kafka。
            eventPublisher.publishEvent(new OrderCreatedEvent(order.id(), order.activityId(), order.userId(), order.quantity()));
            // 返回已创建订单。
            return order;
        } catch (DataIntegrityViolationException e) {
            // 数据库唯一约束冲突时恢复预占库存并清理幂等键。
            compensate(stockKey, idempotencyKey, request.quantity());
            // 返回重复购票或重复请求提示。
            throw new BusinessException(409, "重复购票或请求已提交");
        } catch (RuntimeException e) {
            // 所有运行时失败都恢复 Redis 库存，防止“少卖票”。
            compensate(stockKey, idempotencyKey, request.quantity());
            // 继续抛出原异常，由全局处理器生成响应。
            throw e;
        }
    }

    // 负责在落库失败后补回 Redis 库存。
    private void compensate(String stockKey, String idempotencyKey, int quantity) {
        // 将之前预占的库存增加回来。
        redis.opsForValue().increment(stockKey, quantity);
        // 删除请求标记，让后续合法重试可以继续执行。
        redis.delete(idempotencyKey);
    }
}
