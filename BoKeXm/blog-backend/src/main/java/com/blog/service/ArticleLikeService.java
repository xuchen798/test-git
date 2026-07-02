package com.blog.service;

public interface ArticleLikeService {

    boolean likeArticle(Long userId, Long articleId);

    boolean isLiked(Long userId, Long articleId);

}
