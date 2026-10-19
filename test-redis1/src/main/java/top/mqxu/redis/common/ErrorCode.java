package top.mqxu.redis.common;

import lombok.Getter;

/**
 * 错误码枚举
 *
 * @author mqxu
 */
@Getter
public enum ErrorCode {

    SUCCESS(200, "success"),
    BAD_REQUEST(400, "参数错误"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "系统繁忙，请稍后再试"),

    // 业务异常：短信发送失败（作业新增）
    SEND_MESSAGE_ERROR(30010, "短信发送失败");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}