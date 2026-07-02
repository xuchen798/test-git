package com.blog.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "用户信息VO")
public class UserVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "个性签名")
    private String signature;

    @Schema(description = "个人简介")
    private String bio;

    @Schema(description = "角色")
    private Integer role;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
