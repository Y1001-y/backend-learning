package top.mqxu.toolkit.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import top.mqxu.toolkit.common.Result;
import top.mqxu.toolkit.dto.CaptchaResponse;
import top.mqxu.toolkit.service.CaptchaService;

/**
 * 案例 1：图形验证码
 *
 * @author mqxu
 * @date 2026-10-21
 */
@RestController
@RequestMapping("/toolkit/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final CaptchaService captchaService;

    /**
     * 生成验证码
     *
     * @param type line（默认）/ circle / shear / gif / math
     */
    @GetMapping
    public Result<CaptchaResponse> create(@RequestParam(defaultValue = "line") String type) {
        return Result.ok(captchaService.create(type));
    }

    /**
     * 生成验证码（图片直出版）：响应就是 image/png，Apifox 的"发送"后能直接显示图片。
     * captchaId 和答案放在响应头里，Apifox 切到 Headers 标签可看：
     * X-Captcha-Id 用于校验，X-Debug-Answer 是可直接提交的答案（debug-code 开启时）
     *
     * @param type line（默认）/ circle / shear / gif / math
     */
    @GetMapping(value = "/image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> createImage(@RequestParam(defaultValue = "line") String type) {
        CaptchaService.RawCaptcha raw = captchaService.createRaw(type);
        return ResponseEntity.ok()
                .contentType("gif".equals(raw.style())
                        ? MediaType.parseMediaType("image/gif") : MediaType.IMAGE_PNG)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header("X-Captcha-Id", raw.captchaId())
                .header("X-Debug-Answer", raw.answer() == null ? "" : raw.answer())
                .body(raw.image());
    }

    /**
     * 校验验证码，成功后该 captchaId 立即失效
     */
    @PostMapping("/verify")
    public Result<Boolean> verify(@Valid @RequestBody VerifyRequest request) {
        captchaService.verify(request.captchaId(), request.code());
        return Result.ok("验证通过", true);
    }

    public record VerifyRequest(
            @NotBlank(message = "captchaId 不能为空") String captchaId,
            @NotBlank(message = "验证码不能为空") String code) {
    }
}
