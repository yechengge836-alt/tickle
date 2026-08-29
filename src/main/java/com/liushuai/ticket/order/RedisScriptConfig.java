package com.liushuai.ticket.order;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;

@Configuration
public class RedisScriptConfig {
    /** Atomically reserve stock and mark an idempotency key as pending. */
    @Bean
    DefaultRedisScript<Long> reserveStockScript() {
        String lua = "local stock = redis.call('GET', KEYS[1]); "
                + "if not stock then return -3 end; "
                + "if redis.call('EXISTS', KEYS[2]) == 1 then return 0 end; "
                + "if tonumber(stock) < tonumber(ARGV[1]) then return -1 end; "
                + "redis.call('DECRBY', KEYS[1], ARGV[1]); "
                + "redis.call('SET', KEYS[2], 'PENDING', 'EX', ARGV[2]); return 1;";
        return new DefaultRedisScript<>(lua, Long.class);
    }
}
