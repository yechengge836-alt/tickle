package com.liushuai.ticket.activity;

import com.liushuai.ticket.common.BusinessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ActivityService {
    private static final String STOCK_KEY = "ticket:activity:%d:stock";
    private final ActivityRepository repository;
    private final StringRedisTemplate redis;
    public ActivityService(ActivityRepository repository, StringRedisTemplate redis) {
        this.repository = repository; this.redis = redis;
    }
    public Activity get(long id) { return repository.findById(id).orElseThrow(() -> new BusinessException(404, "活动不存在")); }
    public int getStock(long id) {
        String cached = redis.opsForValue().get(STOCK_KEY.formatted(id));
        if (cached != null) return Integer.parseInt(cached);
        Activity activity = get(id);
        redis.opsForValue().set(STOCK_KEY.formatted(id), String.valueOf(activity.availableStock()));
        return activity.availableStock();
    }
}
