package com.blog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
@Schema(description = "注册DTO")
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "验证码key不能为空")
    @Schema(description = "验证码key", requiredMode = Schema.RequiredMode.REQUIRED)
    private String captchaKey;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "验证码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String captcha;

}
