package com.blog.service;

import com.blog.common.PageResult;
import com.blog.dto.CommentDTO;
import com.blog.vo.CommentVO;

public interface CommentService {

    Long addComment(Long userId, CommentDTO dto);

    void deleteComment(Long userId, Long id);

    PageResult<CommentVO> getCommentList(Long articleId, Long page, Long size);

    void likeComment(Long userId, Long commentId);

}
