package com.blog.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新用户信息DTO")
public class UpdateUserInfoDTO {

    @Schema(description = "用户名（显示名）")
    private String username;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "个性签名")
    private String signature;

    @Schema(description = "个人简介")
    private String bio;

}
