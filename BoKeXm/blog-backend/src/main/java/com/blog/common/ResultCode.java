package com.blog.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录"),
    FORBIDDEN(403, "权限不足"),
    NOT_FOUND(404, "资源不存在"),

    LOGIN_ERROR(1001, "用户名或密码错误"),
    USER_NOT_EXIST(1002, "用户不存在"),
    USER_ALREADY_EXIST(1003, "用户已存在"),
    CAPTCHA_ERROR(1004, "验证码错误"),
    CAPTCHA_EXPIRE(1005, "验证码已过期"),
    TOKEN_INVALID(1006, "登录已失效"),
    TOKEN_EXPIRE(1007, "登录已过期");

    private final Integer code;
    private final String message;

}
