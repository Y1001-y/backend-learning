package top.mqxu.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 绑定 application.yml 里 demo.* 段的业务参数。
 * 字段名与配置项一一对应，Spring 会自动完成属性绑定。
 */
@Data
@ConfigurationProperties(prefix = "demo")
public class DemoProperties {

    /** 缓存相关参数 */
    private Cache cache = new Cache();

    /** 分布式锁相关参数 */
    private Lock lock = new Lock();

    /** 限流相关参数 */
    private Limit limit = new Limit();

    @Data
    public static class Cache {
        /** 手动缓存的默认存活时间，如 10m */
        private String ttl = "10m";
        /** 空值标记的存活时间，通常比正常值短 */
        private String nullTtl = "2m";
        /** TTL 随机抖动幅度（秒），避免大批 key 同时过期 */
        private long jitterSeconds = 30;
    }

    @Data
    public static class Lock {
        /** 抢锁最长等待秒数 */
        private long waitSeconds = 3;
        /** 持锁自动释放秒数 */
        private long leaseSeconds = 10;
    }

    @Data
    public static class Limit {
        /** 限流窗口秒数 */
        private long windowSeconds = 60;
        /** 窗口内允许的请求数 */
        private int maxRequests = 5;
    }
}