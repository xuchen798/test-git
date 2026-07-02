package com.blog.controller;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.entity.SearchHistory;
import com.blog.service.SearchService;
import com.blog.vo.ArticleVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/search")
@Tag(name = "搜索模块")
public class SearchController {

    @Resource
    private SearchService searchService;

    @GetMapping({ "", "/" })
    @Operation(summary = "搜索文章")
    public Result<PageResult<ArticleVO>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long pageNum,
            @RequestParam(required = false) Long pageSize,
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(defaultValue = "global") String type,
            @RequestParam(required = false) Long categoryId,
            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long finalPage = pageNum != null ? pageNum : page;
        Long finalSize = pageSize != null ? pageSize : size;
        PageResult<ArticleVO> result = searchService.search(keyword, finalPage, finalSize, type, userId, categoryId);
        return Result.success(result);
    }

    @GetMapping("/history")
    @Operation(summary = "获取搜索历史")
    public Result<List<SearchHistory>> getSearchHistory(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<SearchHistory> historyList = searchService.getSearchHistory(userId);
        return Result.success(historyList);
    }

    @DeleteMapping("/history")
    @Operation(summary = "清空搜索历史")
    public Result<Void> clearSearchHistory(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        searchService.clearSearchHistory(userId);
        return Result.success();
    }

}
