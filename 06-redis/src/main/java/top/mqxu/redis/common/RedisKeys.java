package top.mqxu.redis.common;

/**
 * Redis key 命名常量。
 */
public class RedisKeys {

    /** 发送间隔限制 key：sms:send:interval:{phone}，SET NX EX 60 */
    public static String sendIntervalKey(String phone) {
        return "sms:send:interval:" + phone;
    }

    /** 当日发送次数 key：sms:send:count:{phone}，INCR 计数 */
    public static String sendCountKey(String phone) {
        return "sms:send:count:" + phone;
    }

    /** 验证码 Hash key：sms:captcha:{phone}，字段 code / status */
    public static String captchaKey(String phone) {
        return "sms:captcha:" + phone;
    }

    /** 验证码错误次数 key：sms:captcha:fail:{phone} */
    public static String captchaFailKey(String phone) {
        return "sms:captcha:fail:" + phone;
    }

    /** Hash 字段名 */
    public static final String FIELD_CODE = "code";
    public static final String FIELD_STATUS = "status";
    public static final String STATUS_UNUSED = "0";
    public static final String STATUS_USED = "1";
}