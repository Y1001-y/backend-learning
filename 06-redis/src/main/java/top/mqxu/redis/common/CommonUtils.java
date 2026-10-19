package top.mqxu.redis.common;

import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * 通用工具：手机号校验、随机验证码生成。
 */
public class CommonUtils {

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^1[3-9]\\d{9}$");

    /** 手机号格式校验 */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /** 生成 4 位随机验证码 */
    public static String generateCode() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(1000, 10000));
    }
}