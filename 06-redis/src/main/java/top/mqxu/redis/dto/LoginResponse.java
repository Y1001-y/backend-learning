package top.mqxu.redis.dto;

import lombok.Data;

@Data
public class LoginResponse {
    /** JWT token */
    private String token;
    /** 手机号 */
    private String phone;
    /** 提示信息 */
    private String message;
    /** token 有效期（秒） */
    private Long expiresIn;
}