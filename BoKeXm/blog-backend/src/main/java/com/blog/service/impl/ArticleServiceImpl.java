package com.blog.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.PageResult;
import com.blog.common.RedisKeyConstants;
import com.blog.common.RedisUtil;
import com.blog.dto.ArticleDTO;
import com.blog.entity.Article;
import com.blog.exception.BusinessException;
import com.blog.mapper.ArticleMapper;
import com.blog.service.ArticleService;
import com.blog.vo.ArticleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ArticleServiceImpl implements ArticleService {

    @Resource
    private ArticleMapper articleMapper;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String ARTICLE_CACHE_PREFIX = "blog:article:detail:";
    private static final String ARTICLE_LIST_CACHE_PREFIX = "blog:article:list:";
    private static final String HOT_ARTICLE_CACHE_KEY = RedisKeyConstants.HOT_ARTICLE;
    private static final String ARTICLE_VIEW_PREFIX = RedisKeyConstants.ARTICLE_VIEW;

    private static final long ARTICLE_DETAIL_EXPIRE = 30;
    private static final long ARTICLE_LIST_EXPIRE = 5;
    private static final long HOT_ARTICLE_EXPIRE = 10;
    private static final long VIEW_COUNT_EXPIRE = 24;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publishArticle(Long userId, ArticleDTO dto) {
        if (userId == null || dto == null) {
            throw new BusinessException("参数错误");
        }

        Article article = new Article();
        BeanUtils.copyProperties(dto, article);
        article.setUserId(userId);
        article.setViewCount(0);
        article.setLikeCount(0);
        article.setFavoriteCount(0);
        article.setCommentCount(0);

        if (article.getStatus() == null) {
            article.setStatus(1);
        }

        articleMapper.insert(article);

        deleteArticleListCache();
        deleteHotArticleCache();

        return article.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticle(Long userId, Long id, ArticleDTO dto) {
        if (userId == null || id == null || dto == null) {
            throw new BusinessException("参数错误");
        }

        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        if (!article.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此文章");
        }

        Article updateArticle = new Article();
        updateArticle.setId(id);
        updateArticle.setTitle(dto.getTitle());
        updateArticle.setContent(dto.getContent());
        updateArticle.setSummary(dto.getSummary());
        updateArticle.setCategoryIds(dto.getCategoryIds());
        updateArticle.setTags(dto.getTags());
        if (dto.getStatus() != null) {
            updateArticle.setStatus(dto.getStatus());
        }

        articleMapper.updateById(updateArticle);

        deleteArticleDetailCache(id);
        deleteArticleListCache();
        deleteHotArticleCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long userId, Long id) {
        if (userId == null || id == null) {
            throw new BusinessException("参数错误");
        }

        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        if (!article.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此文章");
        }

        articleMapper.deleteById(id);

        deleteArticleDetailCache(id);
        deleteArticleListCache();
        deleteHotArticleCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticleStatus(Long userId, Long id, Integer status) {
        if (userId == null || id == null || status == null) {
            throw new BusinessException("参数错误");
        }
        if (status != 0 && status != 1) {
            throw new BusinessException("状态值非法");
        }

        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        if (!article.getUserId().equals(userId)) {
            throw new BusinessException("无权操作此文章");
        }

        Article updateArticle = new Article();
        updateArticle.setId(id);
        updateArticle.setStatus(status);
        articleMapper.updateById(updateArticle);

        deleteArticleDetailCache(id);
        deleteArticleListCache();
        deleteHotArticleCache();
    }

    @Override
    public ArticleVO getArticleById(Long id, Long currentUserId, String ip) {
        if (id == null) {
            throw new BusinessException("参数错误");
        }

        String cacheKey = ARTICLE_CACHE_PREFIX + id;
        Object cacheObj = redisUtil.get(cacheKey);

        ArticleVO articleVO;
        if (cacheObj != null) {
            ArticleVO cachedArticle = (ArticleVO) cacheObj;
            if (cachedArticle.getId() == null) {
                return null;
            }
            articleVO = cachedArticle;
        } else {
            articleVO = articleMapper.getArticleVOById(id);

            if (articleVO == null) {
                redisUtil.set(cacheKey, new ArticleVO(), 60, TimeUnit.SECONDS);
                return null;
            }

            int expireTime = (int) (ARTICLE_DETAIL_EXPIRE * 60 + Math.random() * 10 * 60);
            redisUtil.set(cacheKey, articleVO, expireTime, TimeUnit.MINUTES);
        }

        if (articleVO.getStatus() != null && articleVO.getStatus() != 1) {
            boolean isOwner = currentUserId != null && currentUserId.equals(articleVO.getUserId());
            if (!isOwner) {
                return null;
            }
        }

        incrementViewCount(id, ip);

        return articleVO;
    }

    @Override
    public PageResult<ArticleVO> getArticleList(Long page, Long size, Long categoryId, String keyword) {
        if (page == null || page < 1) {
            page = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }

        String cacheKey = ARTICLE_LIST_CACHE_PREFIX + page + ":" + size + ":" +
                (categoryId != null ? categoryId : "null") + ":" +
                (keyword != null ? keyword : "null");

        Object cacheObj = redisUtil.get(cacheKey);
        if (cacheObj != null) {
            return (PageResult<ArticleVO>) cacheObj;
        }

        Page<ArticleVO> pageParam = new Page<>(page, size);
        IPage<ArticleVO> resultPage = articleMapper.getArticleVOPage(pageParam, categoryId, keyword);

        PageResult<ArticleVO> pageResult = PageResult.of(
                resultPage.getTotal(),
                resultPage.getRecords(),
                page.intValue(),
                size.intValue()
        );

        if (resultPage.getRecords() == null || resultPage.getRecords().isEmpty()) {
            pageResult.setRecords(Collections.emptyList());
            redisUtil.set(cacheKey, pageResult, 60, TimeUnit.SECONDS);
            return pageResult;
        }

        int expireTime = (int) (ARTICLE_LIST_EXPIRE * 60 + Math.random() * 2 * 60);
        redisUtil.set(cacheKey, pageResult, expireTime, TimeUnit.MINUTES);

        return pageResult;
    }

    @Override
    public List<ArticleVO> getHotArticles() {
        Object cacheObj = redisUtil.get(HOT_ARTICLE_CACHE_KEY);
        if (cacheObj != null) {
            return (List<ArticleVO>) cacheObj;
        }

        List<ArticleVO> hotArticles = articleMapper.getHotArticleVOs();

        if (hotArticles == null || hotArticles.isEmpty()) {
            hotArticles = Collections.emptyList();
            redisUtil.set(HOT_ARTICLE_CACHE_KEY, hotArticles, 60, TimeUnit.SECONDS);
            return hotArticles;
        }

        int expireTime = (int) (HOT_ARTICLE_EXPIRE * 60 + Math.random() * 5 * 60);
        redisUtil.set(HOT_ARTICLE_CACHE_KEY, hotArticles, expireTime, TimeUnit.MINUTES);

        return hotArticles;
    }

    @Override
    public PageResult<ArticleVO> getUserArticles(Long userId, Long currentUserId, Long page, Long size) {
        if (userId == null) {
            throw new BusinessException("参数错误");
        }
        if (page == null || page < 1) {
            page = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }

        boolean isOwner = currentUserId != null && currentUserId.equals(userId);
        Integer statusFilter = isOwner ? null : 1;

        Page<ArticleVO> pageParam = new Page<>(page, size);
        IPage<ArticleVO> resultPage = articleMapper.getUserArticleVOPage(pageParam, userId, statusFilter);

        return PageResult.of(
                resultPage.getTotal(),
                resultPage.getRecords() != null ? resultPage.getRecords() : Collections.emptyList(),
                page.intValue(),
                size.intValue()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementViewCount(Long articleId, String ip) {
        if (articleId == null || ip == null) {
            return;
        }

        String viewKey = ARTICLE_VIEW_PREFIX + ip + ":" + articleId;

        Boolean hasViewed = redisUtil.hasKey(viewKey);
        if (hasViewed != null && hasViewed) {
            return;
        }

        Article article = articleMapper.selectById(articleId);
        if (article != null) {
            Article updateArticle = new Article();
            updateArticle.setId(articleId);
            updateArticle.setViewCount(article.getViewCount() == null ? 1 : article.getViewCount() + 1);
            articleMapper.updateById(updateArticle);

            deleteArticleDetailCache(articleId);
            deleteHotArticleCache();
        }

        redisUtil.set(viewKey, "1", VIEW_COUNT_EXPIRE, TimeUnit.HOURS);
    }

    private void deleteArticleDetailCache(Long articleId) {
        String cacheKey = ARTICLE_CACHE_PREFIX + articleId;
        redisUtil.delete(cacheKey);
    }

    private void deleteArticleListCache() {
        try {
            Set<String> keys = redisTemplate.keys(ARTICLE_LIST_CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisUtil.delete(keys);
            }
        } catch (Exception e) {
            log.warn("删除文章列表缓存失败，已降级跳过：{}", e.getMessage());
        }
    }

    private void deleteHotArticleCache() {
        redisUtil.delete(HOT_ARTICLE_CACHE_KEY);
    }

}
