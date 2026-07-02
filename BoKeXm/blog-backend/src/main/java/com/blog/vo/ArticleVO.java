package com.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleVO {

    private Long id;

    private String title;

    private String content;

    private String summary;

    private String categoryIds;

    private String tags;

    private Integer viewCount;

    private Integer likeCount;

    private Integer favoriteCount;

    private Integer commentCount;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Long userId;

    private String username;

    private String nickname;

    private String avatar;

    private String bio;

    private String categoryName;

}
