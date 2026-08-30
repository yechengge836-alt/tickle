// 声明 Redis 脚本配置所在的包。
package com.liushuai.ticket.order;

// 导入 Spring Bean 声明注解。
import org.springframework.context.annotation.Bean;
// 导入配置类注解。
import org.springframework.context.annotation.Configuration;
// 导入 Redis Lua 脚本封装类型。
import org.springframework.data.redis.core.script.DefaultRedisScript;

// 提供下单过程需要的 Redis 脚本 Bean。
@Configuration
public class RedisScriptConfig {
    // 声明“预占库存并写入请求标记”的原子脚本。
    @Bean
    DefaultRedisScript<Long> reserveStockScript() {
        // 拼接 Lua：KEYS[1] 是库存键，KEYS[2] 是幂等键，ARGV 是数量和过期秒数。
        String lua = "local stock = redis.call('GET', KEYS[1]); "
                // 缓存未初始化时返回 -3，让业务层回源 MySQL。
                + "if not stock then return -3 end; "
                // 请求键存在则返回 0，阻止重复提交。
                + "if redis.call('EXISTS', KEYS[2]) == 1 then return 0 end; "
                // 库存不足时返回 -1。
                + "if tonumber(stock) < tonumber(ARGV[1]) then return -1 end; "
                // 以 Redis 单线程原子方式扣减库存。
                + "redis.call('DECRBY', KEYS[1], ARGV[1]); "
                // 写入短期 PENDING 幂等标记，成功后返回 1。
                + "redis.call('SET', KEYS[2], 'PENDING', 'EX', ARGV[2]); return 1;";
        // 指定脚本返回 Long，供业务层判断状态码。
        return new DefaultRedisScript<>(lua, Long.class);
    }
}
