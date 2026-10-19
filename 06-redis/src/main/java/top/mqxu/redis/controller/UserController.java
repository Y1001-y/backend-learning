package top.mqxu.redis.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.mqxu.redis.common.Result;
import top.mqxu.redis.dto.UserInfo;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/user")
public class UserController {

    /** 获取当前登录用户信息：GET /user/me，需带 Bearer token */
    @GetMapping("/me")
    public Result<UserInfo> me(HttpServletRequest request) {
        String phone = (String) request.getAttribute("phone");
        UserInfo info = new UserInfo();
        info.setPhone(phone);
        info.setLoginTime(LocalDateTime.now());
        return Result.ok(info);
    }
}