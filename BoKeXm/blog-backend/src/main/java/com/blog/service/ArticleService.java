package com.blog.service;

import com.blog.common.PageResult;
import com.blog.dto.ArticleDTO;
import com.blog.vo.ArticleVO;

import java.util.List;

public interface ArticleService {

    Long publishArticle(Long userId, ArticleDTO dto);

    void updateArticle(Long userId, Long id, ArticleDTO dto);

    void deleteArticle(Long userId, Long id);

    void updateArticleStatus(Long userId, Long id, Integer status);

    ArticleVO getArticleById(Long id, Long currentUserId, String ip);

    PageResult<ArticleVO> getArticleList(Long page, Long size, Long categoryId, String keyword);

    List<ArticleVO> getHotArticles();

    PageResult<ArticleVO> getUserArticles(Long userId, Long currentUserId, Long page, Long size);

    void incrementViewCount(Long articleId, String ip);

}
