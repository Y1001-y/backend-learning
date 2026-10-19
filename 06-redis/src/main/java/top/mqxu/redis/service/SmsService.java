package top.mqxu.redis.service;

/**
 * 短信服务接口。
 */
public interface SmsService {

    /** 发送验证码 */
    void sendSms(String phone);

    /** 验证码登录，返回 token */
    String login(String phone, String code);
}