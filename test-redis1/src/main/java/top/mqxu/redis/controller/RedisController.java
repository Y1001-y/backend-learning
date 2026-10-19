package top.mqxu.redis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 五种数据类型在 RedisController 里各给了一个接口。
 * 这里只用 StringRedisTemplate，因为演示的都是字符串形式的简单值，无需序列化配置。
 */
@RestController
@RequestMapping("/redis")
@RequiredArgsConstructor
public class RedisController {

    private final StringRedisTemplate stringRedisTemplate;

    /** String：最基础的类型，文本、数字、序列化后的 JSON 都放它里面 */
    @PostMapping("/string")
    public Map<String, Object> setString(@RequestParam String key,
                                         @RequestParam String value,
                                         @RequestParam(defaultValue = "600") long ttlSeconds) {
        stringRedisTemplate.opsForValue().set(key, value, Duration.ofSeconds(ttlSeconds));
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("key", key);
        result.put("value", value);
        result.put("ttlSeconds", stringRedisTemplate.getExpire(key));
        return result;
    }
}