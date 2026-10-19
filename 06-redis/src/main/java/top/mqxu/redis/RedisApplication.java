package top.mqxu.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import top.mqxu.redis.config.DemoProperties;

/**
 * @autor jiehaoren
 * @date 2026/9/23
 * @description RedisApplication
 **/
@SpringBootApplication
@EnableCaching
@EnableConfigurationProperties(DemoProperties.class)
public class RedisApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisApplication.class, args);
    }
}