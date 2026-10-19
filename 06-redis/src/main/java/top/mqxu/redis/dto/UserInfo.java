package top.mqxu.redis.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserInfo {
    /** 手机号 */
    private String phone;
    /** 登录时间 */
    private LocalDateTime loginTime;
}