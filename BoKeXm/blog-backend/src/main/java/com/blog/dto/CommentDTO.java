package com.blog.dto;

import lombok.Data;

@Data
public class CommentDTO {

    private Long articleId;

    private String content;

    private Long parentId;

    private Long replyUserId;

}
