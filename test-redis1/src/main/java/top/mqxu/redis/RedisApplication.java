package top.mqxu.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import top.mqxu.redis.config.CloopenConfig;

@SpringBootApplication
@EnableConfigurationProperties(CloopenConfig.class)
public class RedisApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisApplication.class, args);
    }
}