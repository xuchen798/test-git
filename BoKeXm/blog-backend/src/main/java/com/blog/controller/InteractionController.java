package com.blog.controller;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.service.ArticleLikeService;
import com.blog.service.FavoriteService;
import com.blog.vo.ArticleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/article")
@Tag(name = "互动模块")
public class InteractionController {

    @Resource
    private ArticleLikeService articleLikeService;

    @Resource
    private FavoriteService favoriteService;

    @PostMapping("/{id}/like")
    @Operation(summary = "点赞/取消点赞文章")
    public Result<Map<String, Boolean>> likeArticle(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean liked = articleLikeService.likeArticle(userId, id);
        Map<String, Boolean> result = new HashMap<>();
        result.put("liked", liked);
        return Result.success(result);
    }

    @PostMapping("/{id}/favorite")
    @Operation(summary = "收藏/取消收藏文章")
    public Result<Map<String, Boolean>> favoriteArticle(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean favorited = favoriteService.favoriteArticle(userId, id);
        Map<String, Boolean> result = new HashMap<>();
        result.put("favorited", favorited);
        return Result.success(result);
    }

    @GetMapping({ "/favorite/list", "/favorites" })
    @Operation(summary = "我的收藏列表")
    public Result<PageResult<ArticleVO>> getFavoriteList(
            @RequestParam(required = false) Long pageNum,
            @RequestParam(required = false) Long pageSize,
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long finalPage = pageNum != null ? pageNum : page;
        Long finalSize = pageSize != null ? pageSize : size;
        PageResult<ArticleVO> result = favoriteService.getFavoriteList(userId, finalPage, finalSize);
        return Result.success(result);
    }

    @GetMapping("/{id}/like/status")
    @Operation(summary = "获取点赞状态")
    public Result<Map<String, Boolean>> getLikeStatus(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean liked = articleLikeService.isLiked(userId, id);
        Map<String, Boolean> result = new HashMap<>();
        result.put("liked", liked);
        return Result.success(result);
    }

    @GetMapping("/{id}/favorite/status")
    @Operation(summary = "获取收藏状态")
    public Result<Map<String, Boolean>> getFavoriteStatus(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean favorited = favoriteService.isFavorited(userId, id);
        Map<String, Boolean> result = new HashMap<>();
        result.put("favorited", favorited);
        return Result.success(result);
    }

}
