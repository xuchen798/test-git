package com.blog.service;

import com.blog.common.PageResult;
import com.blog.entity.SearchHistory;
import com.blog.vo.ArticleVO;

import java.util.List;

public interface SearchService {

    PageResult<ArticleVO> search(String keyword, Long page, Long size, String type, Long userId, Long categoryId);

    List<SearchHistory> getSearchHistory(Long userId);

    void clearSearchHistory(Long userId);

    void saveSearchHistory(Long userId, String keyword);

}
