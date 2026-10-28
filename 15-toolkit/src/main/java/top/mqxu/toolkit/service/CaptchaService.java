package top.mqxu.toolkit.service;

import cn.hutool.captcha.AbstractCaptcha;
import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.MathGenerator;
import cn.hutool.core.math.Calculator;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import top.mqxu.toolkit.common.BusinessException;
import top.mqxu.toolkit.common.ErrorCode;
import top.mqxu.toolkit.config.ToolkitProperties;
import top.mqxu.toolkit.dto.CaptchaResponse;

import java.time.Duration;

/**
 * 验证码服务（练习 1：答案存 Redis）
 *
 * @author mqxu
 * @date 2026-10-21
 * @description 答案存 Redis，键 captcha:{id}，TTL 60 秒，校验一次即删除；
 * 失败计数同样放 Redis（captcha:fail:{id}），多实例部署时任何实例都能校验通过
 */
@Service
@RequiredArgsConstructor
public class CaptchaService {

    private static final int MAX_FAILURES = 3;
    private static final String KEY_PREFIX = "captcha:";
    private static final String FAIL_PREFIX = "captcha:fail:";

    private final ToolkitProperties properties;
    private final StringRedisTemplate redis;

    /**
     * 生成验证码（原始字节版）：图片不转 Base64，直接给字节流，
     * 供返回 image/png 的接口使用，Apifox / 浏览器能直接显示
     */
    public RawCaptcha createRaw(String type) {
        String style = StrUtil.blankToDefault(type, "line");
        AbstractCaptcha captcha = switch (style) {
            case "circle" -> CaptchaUtil.createCircleCaptcha(120, 40, 4, 8);
            case "shear" -> CaptchaUtil.createShearCaptcha(120, 40, 4, 2);
            case "gif" -> CaptchaUtil.createGifCaptcha(120, 40, 4);
            case "math" -> new LineCaptcha(120, 40, new MathGenerator(1), 2);
            default -> CaptchaUtil.createLineCaptcha(120, 40, 4, 2);
        };

        String captchaId = IdUtil.fastSimpleUUID();
        Duration ttl = Duration.ofSeconds(properties.getCaptcha().getExpireSeconds());
        redis.opsForValue().set(KEY_PREFIX + captchaId, answer(style, captcha), ttl);

        // 先取 base64（这一步才真正画图），再解码成字节；GIF 是多帧图，头两个字节就是 GIF89a
        byte[] imageBytes = java.util.Base64.getDecoder().decode(captcha.getImageBase64());

        return new RawCaptcha(captchaId, imageBytes,
                properties.getCaptcha().getExpireSeconds(), style, answer(style, captcha));
    }

    /**
     * 原始字节版生成结果
     *
     * @param captchaId     验证码 id
     * @param image         PNG/GIF 图片字节
     * @param expireSeconds 有效秒数
     * @param style         样式
     * @param answer        可提交的答案（debug 用）
     */
    public record RawCaptcha(String captchaId, byte[] image, long expireSeconds,
                             String style, String answer) {
    }

    /**
     * 生成验证码，图片仍由 Hutool 现画，答案写入 Redis
     *
     * @param type line / circle / shear / gif / math
     */
    public CaptchaResponse create(String type) {
        String style = StrUtil.blankToDefault(type, "line");
        AbstractCaptcha captcha = switch (style) {
            case "circle" -> CaptchaUtil.createCircleCaptcha(120, 40, 4, 8);
            case "shear" -> CaptchaUtil.createShearCaptcha(120, 40, 4, 2);
            case "gif" -> CaptchaUtil.createGifCaptcha(120, 40, 4);
            case "math" -> new LineCaptcha(120, 40, new MathGenerator(1), 2);
            default -> CaptchaUtil.createLineCaptcha(120, 40, 4, 2);
        };

        String captchaId = IdUtil.fastSimpleUUID();
        Duration ttl = Duration.ofSeconds(properties.getCaptcha().getExpireSeconds());
        redis.opsForValue().set(KEY_PREFIX + captchaId, answer(style, captcha), ttl);

        // GIF 是多帧图，Base64 前缀是 gif，其余都是 png
        String image = "data:image/" + ("gif".equals(style) ? "gif" : "png")
                + ";base64," + captcha.getImageBase64();

        return new CaptchaResponse(captchaId, image,
                properties.getCaptcha().getExpireSeconds(), style,
                debugAnswer(style, captcha));
    }

    /**
     * 可提交的答案：普通码就是码本身；算术码 code 是算式（如 4*5=），要算出结果
     */
    private String answer(String style, AbstractCaptcha captcha) {
        if (!"math".equals(style)) {
            return captcha.getCode();
        }
        return String.valueOf((int) Calculator.conversion(
                StrUtil.removeSuffix(captcha.getCode(), "=")));
    }

    /**
     * 调试开关打开时返回可直接提交的答案
     */
    private String debugAnswer(String style, AbstractCaptcha captcha) {
        return properties.getCaptcha().isDebugCode() ? answer(style, captcha) : null;
    }

    /**
     * 校验验证码：答案从 Redis 取，成功立即删除；
     * 连错 MAX_FAILURES 次也删除，让这个 captchaId 彻底失效
     */
    public void verify(String captchaId, String code) {
        if (StrUtil.isBlank(captchaId)) {
            throw new BusinessException(ErrorCode.CAPTCHA_NOT_FOUND);
        }
        String expected = redis.opsForValue().get(KEY_PREFIX + captchaId);
        if (expected == null) {
            throw new BusinessException(ErrorCode.CAPTCHA_NOT_FOUND);
        }
        if (StrUtil.isBlank(code)) {
            throw new BusinessException(ErrorCode.CAPTCHA_WRONG, "请输入验证码");
        }

        if (expected.equalsIgnoreCase(code.trim())) {
            redis.delete(KEY_PREFIX + captchaId);
            redis.delete(FAIL_PREFIX + captchaId);
            return;
        }

        Long count = redis.opsForValue().increment(FAIL_PREFIX + captchaId);
        redis.expire(FAIL_PREFIX + captchaId, Duration.ofSeconds(
                properties.getCaptcha().getExpireSeconds()));
        if (count != null && count >= MAX_FAILURES) {
            redis.delete(KEY_PREFIX + captchaId);
            redis.delete(FAIL_PREFIX + captchaId);
            throw new BusinessException(ErrorCode.CAPTCHA_USED, "错误次数过多，验证码已失效");
        }
        throw new BusinessException(ErrorCode.CAPTCHA_WRONG,
                "验证码错误，还可尝试 " + (MAX_FAILURES - count) + " 次");
    }
}
