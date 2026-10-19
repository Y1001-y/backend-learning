package top.mqxu.redis.dto;

import lombok.Data;

@Data
public class UserInfo {
    /** 手机号 */
    private String phone;
    /** 登录时间 */
    private String loginTime;
}