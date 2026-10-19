package top.mqxu.redis.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import top.mqxu.redis.common.BusinessException;
import top.mqxu.redis.common.ErrorCode;
import top.mqxu.redis.common.JwtUtils;

/**
 * 登录态拦截：校验 Authorization: Bearer <token>。
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        String token = auth.substring(7);
        try {
            Claims claims = jwtUtils.parse(token);
            // 把手机号放入 request，供 controller 使用
            request.setAttribute("phone", claims.getSubject());
            return true;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
    }
}