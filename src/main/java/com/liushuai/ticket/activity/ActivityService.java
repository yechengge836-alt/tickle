// 声明活动业务服务所在的包。
package com.liushuai.ticket.activity;

// 导入可携带业务提示的异常类型。
import com.liushuai.ticket.common.BusinessException;
// 导入 Redis 字符串操作模板。
import org.springframework.data.redis.core.StringRedisTemplate;
// 导入 Spring 服务层注解。
import org.springframework.stereotype.Service;

// 注册为 Spring 服务，封装活动和库存的读取逻辑。
@Service
// 定义活动相关业务。
public class ActivityService {
    // 定义活动库存缓存键的格式。
    private static final String STOCK_KEY = "ticket:activity:%d:stock";
    // 保存活动数据库访问对象。
    private final ActivityRepository repository;
    // 保存 Redis 访问对象。
    private final StringRedisTemplate redis;
    // 通过构造器注入依赖，便于测试与维护。
    public ActivityService(ActivityRepository repository, StringRedisTemplate redis) {
        // 保存数据库仓储。
        this.repository = repository;
        // 保存缓存操作模板。
        this.redis = redis;
    }
    // 按活动 ID 查询，不存在时返回明确的业务异常。
    public Activity get(long id) {
        // 将 Optional 的空值转换为 404 业务错误。
        return repository.findById(id).orElseThrow(() -> new BusinessException(404, "活动不存在"));
    }
    // 读取当前可售库存，优先从 Redis 获取。
    public int getStock(long id) {
        // 根据活动 ID 生成缓存键并获取缓存值。
        String cached = redis.opsForValue().get(STOCK_KEY.formatted(id));
        // 缓存存在时直接转换为整数返回。
        if (cached != null) {
            return Integer.parseInt(cached);
        }
        // 缓存未命中时回源查询数据库。
        Activity activity = get(id);
        // 将数据库库存写回缓存，为后续查询与扣减预热。
        redis.opsForValue().set(STOCK_KEY.formatted(id), String.valueOf(activity.availableStock()));
        // 返回数据库中的最新库存。
        return activity.availableStock();
    }
}
