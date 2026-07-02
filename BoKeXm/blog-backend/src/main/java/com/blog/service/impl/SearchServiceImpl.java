package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.PageResult;
import com.blog.common.RedisKeyConstants;
import com.blog.common.RedisUtil;
import com.blog.entity.SearchHistory;
import com.blog.exception.BusinessException;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.SearchHistoryMapper;
import com.blog.service.SearchService;
import com.blog.vo.ArticleVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SearchServiceImpl implements SearchService {

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private SearchHistoryMapper searchHistoryMapper;

    @Resource
    private RedisUtil redisUtil;

    private static final String SEARCH_HISTORY_CACHE_PREFIX = RedisKeyConstants.SEARCH_HISTORY;
    private static final long SEARCH_HISTORY_EXPIRE = 7;

    @Override
    public PageResult<ArticleVO> search(String keyword, Long page, Long size, String type, Long userId, Long categoryId) {
        if (page == null || page < 1) {
            page = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }
        if (type == null || type.isEmpty()) {
            type = "global";
        }

        Page<ArticleVO> pageParam = new Page<>(page, size);
        IPage<ArticleVO> resultPage;

        if ("personal".equals(type)) {
            if (userId == null) {
                throw new BusinessException("个人搜索需要登录");
            }
            resultPage = articleMapper.searchPersonalArticles(pageParam, userId, keyword, categoryId);
        } else {
            resultPage = articleMapper.searchGlobalArticles(pageParam, keyword, categoryId);
        }

        if (userId != null && keyword != null && !keyword.isEmpty()) {
            saveSearchHistory(userId, keyword);
        }

        return PageResult.of(
                resultPage.getTotal(),
                resultPage.getRecords() != null ? resultPage.getRecords() : Collections.emptyList(),
                page.intValue(),
                size.intValue()
        );
    }

    @Override
    public List<SearchHistory> getSearchHistory(Long userId) {
        if (userId == null) {
            throw new BusinessException("参数错误");
        }

        String cacheKey = SEARCH_HISTORY_CACHE_PREFIX + userId;
        Object cacheObj = redisUtil.get(cacheKey);

        if (cacheObj != null) {
            return (List<SearchHistory>) cacheObj;
        }

        LambdaQueryWrapper<SearchHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SearchHistory::getUserId, userId)
                .orderByDesc(SearchHistory::getCreateTime)
                .last("LIMIT 10");

        List<SearchHistory> historyList = searchHistoryMapper.selectList(queryWrapper);

        if (historyList == null) {
            historyList = Collections.emptyList();
        }

        redisUtil.set(cacheKey, historyList, SEARCH_HISTORY_EXPIRE, TimeUnit.DAYS);

        return historyList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearSearchHistory(Long userId) {
        if (userId == null) {
            throw new BusinessException("参数错误");
        }

        LambdaQueryWrapper<SearchHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SearchHistory::getUserId, userId);
        searchHistoryMapper.delete(queryWrapper);

        String cacheKey = SEARCH_HISTORY_CACHE_PREFIX + userId;
        redisUtil.delete(cacheKey);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSearchHistory(Long userId, String keyword) {
        if (userId == null || keyword == null || keyword.isEmpty()) {
            return;
        }

        LambdaQueryWrapper<SearchHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SearchHistory::getUserId, userId)
                .eq(SearchHistory::getKeyword, keyword);

        SearchHistory existing = searchHistoryMapper.selectOne(queryWrapper);
        if (existing != null) {
            existing.setCreateTime(LocalDateTime.now());
            searchHistoryMapper.updateById(existing);
        } else {
            SearchHistory searchHistory = new SearchHistory();
            searchHistory.setUserId(userId);
            searchHistory.setKeyword(keyword);
            searchHistory.setCreateTime(LocalDateTime.now());
            searchHistoryMapper.insert(searchHistory);
        }

        String cacheKey = SEARCH_HISTORY_CACHE_PREFIX + userId;
        redisUtil.delete(cacheKey);
    }

}
