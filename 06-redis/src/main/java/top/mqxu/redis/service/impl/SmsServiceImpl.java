package top.mqxu.redis.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import top.mqxu.redis.common.*;
import top.mqxu.redis.config.SmsProperties;
import top.mqxu.redis.service.SmsService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsServiceImpl implements SmsService {

    private final RedisCache redisCache;
    private final SmsProperties smsProperties;
    private final JwtUtils jwtUtils;

    @Override
    public void sendSms(String phone) {
        // 1. 校验手机号格式
        if (!CommonUtils.isValidPhone(phone)) {
            throw new BusinessException(ErrorCode.PHONE_ERROR);
        }

        // 2. 60 秒内只放一次（SET NX EX）
        boolean allow = redisCache.setIfAbsent(
                RedisKeys.sendIntervalKey(phone),
                "1",
                smsProperties.getSendIntervalSeconds());
        if (!allow) {
            throw new BusinessException(ErrorCode.PHONE_CODE_SEND_TOO_MANY);
        }

        // 3. 当日发送次数 ≤ 10（INCR，按自然日过期）
        String countKey = RedisKeys.sendCountKey(phone);
        Long count = redisCache.increment(countKey);
        if (count != null && count == 1) {
            // 首次计数，设置到当天 23:59:59 过期
            redisCache.expire(countKey, secondsUntilEndOfDay());
        }
        if (count != null && count > smsProperties.getSendDailyLimit()) {
            throw new BusinessException(ErrorCode.PHONE_CODE_SEND_LIMIT);
        }

        // 4. 生成 4 位验证码
        String code = CommonUtils.generateCode();

        // 5. mock 模式下只写日志，不调用短信 SDK
        if (smsProperties.isMockEnabled()) {
            log.info("【mock】向手机号 {} 发送验证码：{}", phone, code);
        } else {
            // 调用容联云真实发送（此处省略，避免依赖容联云 SDK 细节）
            log.warn("mock-enabled=false，需接入真实容联云短信发送");
            // TODO: 调用 cloopen sdk 发送
        }

        // 6. 验证码存 Redis Hash：sms:captcha:{phone}，code / status=0，EXPIRE 300
        Map<String, String> captcha = new HashMap<>();
        captcha.put(RedisKeys.FIELD_CODE, code);
        captcha.put(RedisKeys.FIELD_STATUS, RedisKeys.STATUS_UNUSED);
        redisCache.putAll(RedisKeys.captchaKey(phone), captcha);
        redisCache.expire(RedisKeys.captchaKey(phone), smsProperties.getCodeExpireSeconds());
    }

    @Override
    public String login(String phone, String code) {
        // 1. 校验
        if (!CommonUtils.isValidPhone(phone)) {
            throw new BusinessException(ErrorCode.PHONE_ERROR);
        }
        if (code == null || code.isEmpty()) {
            throw new BusinessException(ErrorCode.PHONE_CODE_EMPTY);
        }

        // 2. 从 Redis 取验证码
        String captchaKey = RedisKeys.captchaKey(phone);
        Map<Object, Object> captcha = redisCache.entries(captchaKey);
        if (captcha == null || captcha.isEmpty()) {
            throw new BusinessException(ErrorCode.PHONE_CODE_EXPIRED);
        }

        String storedCode = String.valueOf(captcha.get(RedisKeys.FIELD_CODE));
        String status = String.valueOf(captcha.get(RedisKeys.FIELD_STATUS));

        // 3. status=1 → 验证码已使用
        if (RedisKeys.STATUS_USED.equals(status)) {
            throw new BusinessException(ErrorCode.PHONE_CODE_USED);
        }

        // 4. code 不一致 → 错误次数 +1，够 5 次作废
        if (!storedCode.equals(code)) {
            String failKey = RedisKeys.captchaFailKey(phone);
            Long failCount = redisCache.increment(failKey);
            redisCache.expire(failKey, smsProperties.getCodeExpireSeconds());
            if (failCount != null && failCount >= smsProperties.getMaxFailCount()) {
                redisCache.delete(captchaKey);
                throw new BusinessException(ErrorCode.PHONE_CODE_ERROR_TOO_MANY);
            }
            throw new BusinessException(ErrorCode.PHONE_CODE_ERROR);
        }

        // 5. code 一致 → status 置 1（不删 key，区分三种状态）
        redisCache.put(captchaKey, RedisKeys.FIELD_STATUS, RedisKeys.STATUS_USED);

        // 6. 签发 JWT，sub = 手机号
        return jwtUtils.create(phone);
    }

    /** 距当天 23:59:59 的剩余秒数 */
    private long secondsUntilEndOfDay() {
        LocalDate today = LocalDate.now();
        long todaySeconds = today.atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault())
                .toEpochSecond() - System.currentTimeMillis() / 1000;
        return Math.max(todaySeconds, 1);
    }
}