package com.liushuai.ticket.order;

import com.liushuai.ticket.activity.ActivityRepository;
import com.liushuai.ticket.activity.ActivityService;
import com.liushuai.ticket.common.BusinessException;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private static final String STOCK_KEY = "ticket:activity:%d:stock";
    private static final String IDEMPOTENCY_KEY = "ticket:request:%s";
    private final ActivityService activityService;
    private final ActivityRepository activityRepository;
    private final OrderRepository orderRepository;
    private final StringRedisTemplate redis;
    private final DefaultRedisScript<Long> reserveStockScript;
    private final ApplicationEventPublisher eventPublisher;

    public OrderService(ActivityService activityService, ActivityRepository activityRepository,
                        OrderRepository orderRepository, StringRedisTemplate redis,
                        DefaultRedisScript<Long> reserveStockScript, ApplicationEventPublisher eventPublisher) {
        this.activityService = activityService; this.activityRepository = activityRepository;
        this.orderRepository = orderRepository; this.redis = redis;
        this.reserveStockScript = reserveStockScript; this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TicketOrder create(CreateOrderRequest request) {
        var existingRequest = orderRepository.findByRequestId(request.requestId());
        if (existingRequest.isPresent()) return existingRequest.get();
        orderRepository.findByUserAndActivity(request.userId(), request.activityId())
                .ifPresent(order -> { throw new BusinessException(409, "同一活动每位用户只能购买一次"); });
        activityService.get(request.activityId());

        String stockKey = STOCK_KEY.formatted(request.activityId());
        String idempotencyKey = IDEMPOTENCY_KEY.formatted(request.requestId());
        Long result = redis.execute(reserveStockScript, List.of(stockKey, idempotencyKey), String.valueOf(request.quantity()), "300");
        if (result == null || result == -3) {
            // Cache miss is repaired from MySQL, then the caller retries once.
            redis.opsForValue().set(stockKey, String.valueOf(activityService.get(request.activityId()).availableStock()));
            throw new BusinessException(503, "库存缓存已刷新，请重试");
        }
        if (result == 0) throw new BusinessException(409, "请求正在处理，请勿重复提交");
        if (result == -1) throw new BusinessException(409, "余票不足");

        try {
            orderRepository.insert(request);
            if (activityRepository.decreaseDbStock(request.activityId(), request.quantity()) != 1) {
                throw new BusinessException(409, "余票不足");
            }
            TicketOrder order = orderRepository.findByRequestId(request.requestId()).orElseThrow();
            redis.opsForValue().set(idempotencyKey, String.valueOf(order.id()));
            eventPublisher.publishEvent(new OrderCreatedEvent(order.id(), order.activityId(), order.userId(), order.quantity()));
            return order;
        } catch (DataIntegrityViolationException e) {
            compensate(stockKey, idempotencyKey, request.quantity());
            throw new BusinessException(409, "重复购票或请求已提交");
        } catch (RuntimeException e) {
            compensate(stockKey, idempotencyKey, request.quantity());
            throw e;
        }
    }

    private void compensate(String stockKey, String idempotencyKey, int quantity) {
        redis.opsForValue().increment(stockKey, quantity);
        redis.delete(idempotencyKey);
    }
}
