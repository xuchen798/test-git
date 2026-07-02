package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.entity.Article;
import com.blog.entity.ArticleLike;
import com.blog.exception.BusinessException;
import com.blog.mapper.ArticleLikeMapper;
import com.blog.mapper.ArticleMapper;
import com.blog.service.ArticleLikeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

@Service
public class ArticleLikeServiceImpl implements ArticleLikeService {

    @Resource
    private ArticleLikeMapper articleLikeMapper;

    @Resource
    private ArticleMapper articleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean likeArticle(Long userId, Long articleId) {
        if (userId == null || articleId == null) {
            throw new BusinessException("参数错误");
        }

        Article article = articleMapper.selectById(articleId);
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        LambdaQueryWrapper<ArticleLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleLike::getUserId, userId)
                .eq(ArticleLike::getArticleId, articleId);
        ArticleLike existLike = articleLikeMapper.selectOne(wrapper);

        Article updateArticle = new Article();
        updateArticle.setId(articleId);

        if (existLike != null) {
            articleLikeMapper.deleteById(existLike.getId());
            int newCount = Math.max(0, (article.getLikeCount() == null ? 0 : article.getLikeCount()) - 1);
            updateArticle.setLikeCount(newCount);
            articleMapper.updateById(updateArticle);
            return false;
        } else {
            ArticleLike articleLike = new ArticleLike();
            articleLike.setUserId(userId);
            articleLike.setArticleId(articleId);
            articleLikeMapper.insert(articleLike);
            updateArticle.setLikeCount((article.getLikeCount() == null ? 0 : article.getLikeCount()) + 1);
            articleMapper.updateById(updateArticle);
            return true;
        }
    }

    @Override
    public boolean isLiked(Long userId, Long articleId) {
        if (userId == null || articleId == null) {
            return false;
        }

        LambdaQueryWrapper<ArticleLike> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ArticleLike::getUserId, userId)
                .eq(ArticleLike::getArticleId, articleId);
        Long count = articleLikeMapper.selectCount(wrapper);
        return count != null && count > 0;
    }

}
