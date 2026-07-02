package com.blog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录VO")
public class LoginVO {

    @Schema(description = "token")
    private String token;

    @Schema(description = "用户信息")
    private UserVO userInfo;

}
