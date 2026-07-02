package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.PageResult;
import com.blog.dto.CommentDTO;
import com.blog.entity.Article;
import com.blog.entity.Comment;
import com.blog.exception.BusinessException;
import com.blog.mapper.ArticleMapper;
import com.blog.mapper.CommentMapper;
import com.blog.service.CommentService;
import com.blog.vo.CommentVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Resource
    private CommentMapper commentMapper;

    @Resource
    private ArticleMapper articleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addComment(Long userId, CommentDTO dto) {
        if (userId == null || dto == null || dto.getArticleId() == null) {
            throw new BusinessException("参数错误");
        }
        if (dto.getContent() == null || dto.getContent().trim().isEmpty()) {
            throw new BusinessException("评论内容不能为空");
        }

        Article article = articleMapper.selectById(dto.getArticleId());
        if (article == null) {
            throw new BusinessException("文章不存在");
        }

        Comment comment = new Comment();
        comment.setArticleId(dto.getArticleId());
        comment.setUserId(userId);
        comment.setContent(dto.getContent());
        comment.setLikeCount(0);

        if (dto.getParentId() != null && dto.getParentId() > 0) {
            Comment parentComment = commentMapper.selectById(dto.getParentId());
            if (parentComment == null) {
                throw new BusinessException("父评论不存在");
            }
            comment.setParentId(dto.getParentId());
            comment.setReplyUserId(dto.getReplyUserId());
        } else {
            comment.setParentId(0L);
        }

        commentMapper.insert(comment);

        Article updateArticle = new Article();
        updateArticle.setId(dto.getArticleId());
        updateArticle.setCommentCount(article.getCommentCount() == null ? 1 : article.getCommentCount() + 1);
        articleMapper.updateById(updateArticle);

        return comment.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long userId, Long id) {
        if (userId == null || id == null) {
            throw new BusinessException("参数错误");
        }

        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此评论");
        }

        int deletedCount = deleteCommentWithChildren(id);

        Article article = articleMapper.selectById(comment.getArticleId());
        if (article != null) {
            Article updateArticle = new Article();
            updateArticle.setId(comment.getArticleId());
            int newCount = Math.max(0, (article.getCommentCount() == null ? 0 : article.getCommentCount()) - deletedCount);
            updateArticle.setCommentCount(newCount);
            articleMapper.updateById(updateArticle);
        }
    }

    private int deleteCommentWithChildren(Long commentId) {
        int count = 1;
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getParentId, commentId);
        List<Comment> children = commentMapper.selectList(wrapper);
        for (Comment child : children) {
            count += deleteCommentWithChildren(child.getId());
        }
        commentMapper.deleteById(commentId);
        return count;
    }

    @Override
    public PageResult<CommentVO> getCommentList(Long articleId, Long page, Long size) {
        if (articleId == null) {
            throw new BusinessException("参数错误");
        }
        if (page == null || page < 1) {
            page = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }

        LambdaQueryWrapper<Comment> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(Comment::getArticleId, articleId)
                .eq(Comment::getParentId, 0);
        Long total = commentMapper.selectCount(countWrapper);

        List<CommentVO> topLevelComments = commentMapper.getTopLevelComments(articleId);

        int start = (page.intValue() - 1) * size.intValue();
        int end = Math.min(start + size.intValue(), topLevelComments.size());
        List<CommentVO> pageComments = new ArrayList<>();
        if (start < topLevelComments.size()) {
            pageComments = topLevelComments.subList(start, end);
        }

        List<CommentVO> resultList = new ArrayList<>();
        for (CommentVO commentVO : pageComments) {
            fillChildren(commentVO);
            resultList.add(commentVO);
        }

        return PageResult.of(total, resultList, page.intValue(), size.intValue());
    }

    private void fillChildren(CommentVO parent) {
        List<CommentVO> children = commentMapper.getChildComments(parent.getId());
        if (children != null && !children.isEmpty()) {
            for (CommentVO child : children) {
                fillChildren(child);
            }
            parent.setChildren(children);
        } else {
            parent.setChildren(Collections.emptyList());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeComment(Long userId, Long commentId) {
        if (userId == null || commentId == null) {
            throw new BusinessException("参数错误");
        }

        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        Comment updateComment = new Comment();
        updateComment.setId(commentId);
        updateComment.setLikeCount((comment.getLikeCount() == null ? 0 : comment.getLikeCount()) + 1);
        commentMapper.updateById(updateComment);
    }

}
