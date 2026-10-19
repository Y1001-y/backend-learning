package top.mqxu.redis.common;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis 读写封装，基于 StringRedisTemplate。
 */
@Component
public class RedisCache {

    private final StringRedisTemplate redisTemplate;

    public RedisCache(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /** 设置字符串并指定过期时间（单位：秒） */
    public void set(String key, String value, long seconds) {
        redisTemplate.opsForValue().set(key, value, seconds, TimeUnit.SECONDS);
    }

    /** 只有 key 不存在时才设置成功（SET NX），用于限流/间隔 */
    public boolean setIfAbsent(String key, String value, long seconds) {
        Boolean ok = redisTemplate.opsForValue()
                .setIfAbsent(key, value, seconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(ok);
    }

    /** 自增，并设置过期时间（从当天零点开始计算） */
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /** 设置 key 过期时间（秒） */
    public void expire(String key, long seconds) {
        redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
    }

    /** 写入 Hash（HMSET），code 和 status 两个字段 */
    public void putAll(String key, Map<String, String> map) {
        redisTemplate.opsForHash().putAll(key, map);
    }

    /** 读取整个 Hash（HGETALL），返回 Map */
    public Map<Object, Object> entries(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /** 设置 Hash 中某个字段的值（HSET） */
    public void put(String key, String field, String value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /** 获取 Hash 中某个字段的值（HGET） */
    public String get(String key, String field) {
        Object v = redisTemplate.opsForHash().get(key, field);
        return v == null ? null : v.toString();
    }

    /** 删除 key */
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}