package top.mqxu.redis.dto;

import lombok.Data;

@Data
public class LoginRequest {
    /** 手机号 */
    private String phone;
    /** 验证码 */
    private String code;
}