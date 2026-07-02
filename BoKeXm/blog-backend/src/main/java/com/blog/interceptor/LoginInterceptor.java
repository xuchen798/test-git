package com.blog.interceptor;

import com.blog.common.JwtUtil;
import com.blog.common.RedisKeyConstants;
import com.blog.common.RedisUtil;
import com.blog.common.ResultCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.blog.common.Result;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 公开接口放行：文章详情 /article/{数字} 仅 GET 公开
        String uri = request.getRequestURI();
        String method = request.getMethod();
        if (isPublicArticleDetail(uri, method) || isPublicUserArticles(uri, method)) {
            return true;
        }

        String token = request.getHeader(jwtUtil.getHeader());
        if (token != null && token.startsWith(jwtUtil.getPrefix())) {
            token = token.substring(jwtUtil.getPrefix().length());
        }

        if (token == null || token.isEmpty()) {
            writeErrorResponse(response, ResultCode.UNAUTHORIZED);
            return false;
        }

        if (!jwtUtil.validateToken(token)) {
            writeErrorResponse(response, ResultCode.TOKEN_EXPIRE);
            return false;
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            writeErrorResponse(response, ResultCode.TOKEN_INVALID);
            return false;
        }

        String redisKey = RedisKeyConstants.USER_LOGIN_TOKEN + userId;
        Object redisToken = redisUtil.get(redisKey);
        if (redisToken == null) {
            // Redis 不可用或 token 未写入 Redis，降级处理：只要 JWT 本身有效就放行，避免 Redis 挂掉时整个系统无法登录
            request.setAttribute("userId", userId);
            request.setAttribute("username", jwtUtil.getUsernameFromToken(token));
            return true;
        }
        if (!token.equals(redisToken.toString())) {
            writeErrorResponse(response, ResultCode.UNAUTHORIZED);
            return false;
        }

        request.setAttribute("userId", userId);
        request.setAttribute("username", jwtUtil.getUsernameFromToken(token));
        return true;
    }

    private boolean isPublicArticleDetail(String uri, String method) {
        return "GET".equalsIgnoreCase(method) && uri.matches("/article/\\d+");
    }

    private boolean isPublicUserArticles(String uri, String method) {
        return "GET".equalsIgnoreCase(method) && uri.matches("/article/user/\\d+");
    }

    private void writeErrorResponse(HttpServletResponse response, ResultCode resultCode) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        ObjectMapper mapper = new ObjectMapper();
        Result<?> result = Result.error(resultCode);
        response.getWriter().write(mapper.writeValueAsString(result));
    }

}
