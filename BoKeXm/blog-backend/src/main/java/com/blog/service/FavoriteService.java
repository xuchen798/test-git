package com.blog.service;

import com.blog.common.PageResult;
import com.blog.vo.ArticleVO;

public interface FavoriteService {

    boolean favoriteArticle(Long userId, Long articleId);

    boolean isFavorited(Long userId, Long articleId);

    PageResult<ArticleVO> getFavoriteList(Long userId, Long page, Long size);

}
