package top.mqxu.redis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.mqxu.redis.common.Result;
import top.mqxu.redis.dto.LoginRequest;
import top.mqxu.redis.dto.LoginResponse;
import top.mqxu.redis.service.SmsService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;

    /** 发送验证码：POST /sms/send?phone=xxx */
    @PostMapping("/send")
    public Result<Map<String, Object>> send(@RequestParam String phone) {
        smsService.sendSms(phone);
        Map<String, Object> data = new HashMap<>();
        data.put("message", "短信发送成功");
        return Result.ok(data);
    }

    /** 验证码登录：POST /sms/login {phone, code} */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        String token = smsService.login(request.getPhone(), request.getCode());
        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setPhone(request.getPhone());
        resp.setMessage("登录成功");
        resp.setExpiresIn(7200L);
        return Result.ok(resp);
    }
}