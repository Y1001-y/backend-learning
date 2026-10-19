package top.mqxu.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 短信策略参数绑定，对应 application.yml 的 mqxu.sms 前缀。
 */
@Data
@Component
@ConfigurationProperties(prefix = "mqxu.sms")
public class SmsProperties {

    /** mock 开关：true 时不调用短信 SDK */
    private boolean mockEnabled;

    /** 验证码有效期（秒） */
    private Integer codeExpireSeconds;

    /** 两次发送最小间隔（秒） */
    private Integer sendIntervalSeconds;

    /** 每日发送上限 */
    private Integer sendDailyLimit;

    /** 验证码最大错误次数 */
    private Integer maxFailCount;
}