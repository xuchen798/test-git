package com.blog.dto;

import lombok.Data;

@Data
public class ArticleDTO {

    private String title;

    private String content;

    private String summary;

    private String categoryIds;

    private String tags;

    private Integer status;

}
