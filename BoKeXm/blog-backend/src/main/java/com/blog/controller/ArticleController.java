package com.blog.controller;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.dto.ArticleDTO;
import com.blog.service.ArticleService;
import com.blog.service.CategoryService;
import com.blog.vo.ArticleVO;
import com.blog.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/article")
@Tag(name = "文章模块")
public class ArticleController {

    @Resource
    private ArticleService articleService;

    @Resource
    private CategoryService categoryService;

    @Value("${file.upload.path:D:/blog/upload/}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:/upload/}")
    private String uploadUrlPrefix;

    @GetMapping("/list")
    @Operation(summary = "获取文章列表")
    public Result<PageResult<ArticleVO>> getArticleList(
            @RequestParam(value = "pageNum", defaultValue = "1") Long pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        PageResult<ArticleVO> result = articleService.getArticleList(pageNum, pageSize, categoryId, keyword);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取文章详情")
    public Result<ArticleVO> getArticleById(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        String ip = getClientIp(request);
        ArticleVO articleVO = articleService.getArticleById(id, currentUserId, ip);
        return Result.success(articleVO);
    }

    @PostMapping({ "", "/publish" })
    @Operation(summary = "发布文章")
    public Result<Long> publishArticle(@RequestBody ArticleDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long articleId = articleService.publishArticle(userId, dto);
        return Result.success(articleId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新文章")
    public Result<Void> updateArticle(@PathVariable Long id,
                                      @RequestBody ArticleDTO dto,
                                      HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        articleService.updateArticle(userId, id, dto);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新文章状态（公开/隐藏）")
    public Result<Void> updateArticleStatus(@PathVariable Long id,
                                            @RequestParam Integer status,
                                            HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        articleService.updateArticleStatus(userId, id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除文章")
    public Result<Void> deleteArticle(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        articleService.deleteArticle(userId, id);
        return Result.success();
    }

    @GetMapping("/hot")
    @Operation(summary = "获取热门文章")
    public Result<List<ArticleVO>> getHotArticles() {
        List<ArticleVO> hotArticles = articleService.getHotArticles();
        return Result.success(hotArticles);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户文章列表")
    public Result<PageResult<ArticleVO>> getUserArticles(
            @PathVariable Long userId,
            @RequestParam(value = "pageNum", defaultValue = "1") Long pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Long pageSize,
            HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        PageResult<ArticleVO> result = articleService.getUserArticles(userId, currentUserId, pageNum, pageSize);
        return Result.success(result);
    }

    @PostMapping("/upload/image")
    @Operation(summary = "上传文章图片")
    public Result<String> uploadArticleImage(@RequestParam("image") MultipartFile file,
                                             HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        if (file == null || file.isEmpty()) {
            return Result.error("图片文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = "";
        if (originalFilename != null && originalFilename.lastIndexOf(".") != -1) {
            suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID() + suffix;

        File destDir = new File(uploadPath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        File destFile = new File(destDir, filename);
        try {
            file.transferTo(destFile);
        } catch (IOException e) {
            return Result.error("图片上传失败：" + e.getMessage());
        }

        String imageUrl = uploadUrlPrefix + filename;
        return Result.success(imageUrl);
    }

    @GetMapping({ "/category/list", "/categories" })
    @Operation(summary = "获取分类列表")
    public Result<List<CategoryVO>> listAllCategories() {
        List<CategoryVO> categories = categoryService.listAllCategories();
        return Result.success(categories);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

}
