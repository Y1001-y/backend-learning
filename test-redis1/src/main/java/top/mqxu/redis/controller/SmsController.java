package top.mqxu.redis.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.mqxu.redis.common.BusinessException;
import top.mqxu.redis.common.ErrorCode;
import top.mqxu.redis.common.Result;
import top.mqxu.redis.service.SmsService;

@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsService smsService;

    @PostMapping("/send")
    public Result<Void> send(@RequestParam String phone) {
        boolean ok = smsService.sendSms(phone);
        if (!ok) {
            throw new BusinessException(ErrorCode.SEND_MESSAGE_ERROR);
        }
        return Result.ok();
    }
}