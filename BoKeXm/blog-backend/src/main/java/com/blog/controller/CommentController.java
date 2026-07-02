package com.blog.controller;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.CommentDTO;
import com.blog.service.CommentService;
import com.blog.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/comment")
@Tag(name = "评论模块")
public class CommentController {

    @Resource
    private CommentService commentService;

    @PostMapping({ "", "/add" })
    @Operation(summary = "添加评论")
    public Result<Long> addComment(@RequestBody CommentDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long commentId = commentService.addComment(userId, dto);
        return Result.success(commentId);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除评论")
    public Result<Void> deleteComment(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        commentService.deleteComment(userId, id);
        return Result.success();
    }

    @GetMapping("/list")
    @Operation(summary = "获取评论列表")
    public Result<PageResult<CommentVO>> getCommentList(
            @RequestParam Long articleId,
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size) {
        PageResult<CommentVO> result = commentService.getCommentList(articleId, page, size);
        return Result.success(result);
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "点赞评论")
    public Result<Void> likeComment(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        commentService.likeComment(userId, id);
        return Result.success();
    }

}
