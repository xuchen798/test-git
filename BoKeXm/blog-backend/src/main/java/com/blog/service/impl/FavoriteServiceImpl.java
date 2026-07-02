package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.PageResult;
import com.blog.entity.Article;
import com.blog.entity.Favorite;
import com.blog.exception.BusinessException;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.FavoriteMapper;
import com.blog.service.FavoriteService;
import com.blog.vo.ArticleVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.Collections;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Resource
    private FavoriteMapper favoriteMapper;

    @Resource
    private ArticleMapper articleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean favoriteArticle(Long userId, Long articleId) {
        if (userId == null || articleId == null) {
            throw new BusinessException("参数错误");
        }

        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getArticleId, articleId);
        Favorite existFavorite = favoriteMapper.selectOne(wrapper);

        Article updateArticle = new Article();
        updateArticle.setId(articleId);

        if (existFavorite != null) {
            favoriteMapper.deleteById(existFavorite.getId());
            int newCount = Math.max(0, (article.getFavoriteCount() == null ? 0 : article.getFavoriteCount()) - 1);
            updateArticle.setFavoriteCount(newCount);
            articleMapper.updateById(updateArticle);
            return false;
        } else {
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setArticleId(articleId);
            favoriteMapper.insert(favorite);
            updateArticle.setFavoriteCount((article.getFavoriteCount() == null ? 0 : article.getFavoriteCount()) + 1);
            articleMapper.updateById(updateArticle);
            return true;
        }
    }

    @Override
    public boolean isFavorited(Long userId, Long articleId) {
        if (userId == null || articleId == null) {
            return false;
        }

        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Favorite::getUserId, userId)
                .eq(Favorite::getArticleId, articleId);
        Long count = favoriteMapper.selectCount(wrapper);
        return count != null && count > 0;
    }

    @Override
    public PageResult<ArticleVO> getFavoriteList(Long userId, Long page, Long size) {
        if (userId == null) {
            throw new BusinessException("参数错误");
        }
        if (page == null || page < 1) {
            page = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }

        Page<ArticleVO> pageParam = new Page<>(page, size);
        IPage<ArticleVO> resultPage = favoriteMapper.getFavoriteArticlePage(pageParam, userId);

        return PageResult.of(
                resultPage.getTotal(),
                resultPage.getRecords() != null ? resultPage.getRecords() : Collections.emptyList(),
                page.intValue(),
                size.intValue()
        );
    }

}
