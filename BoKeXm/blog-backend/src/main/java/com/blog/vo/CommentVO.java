package com.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {

    private Long id;

    private Long articleId;

    private Long userId;

    private String username;

    private String avatar;

    private Long parentId;

    private Long replyUserId;

    private String replyUsername;

    private String replyAvatar;

    private String content;

    private Integer likeCount;

    private LocalDateTime createTime;

    private List<CommentVO> children;

}
