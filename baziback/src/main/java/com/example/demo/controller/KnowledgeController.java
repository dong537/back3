package com.example.demo.controller;

import com.example.demo.dto.response.Result;
import com.example.demo.entity.KnowledgeArticle;
import com.example.demo.entity.KnowledgeCategory;
import com.example.demo.service.KnowledgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 知识库控制器
 */
@Slf4j
@Tag(name = "知识库管理", description = "知识库分类和文章的查询")
@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {
    
    private final KnowledgeService knowledgeService;
    
    /**
     * 获取所有分类
     */
    @Operation(summary = "获取分类列表", description = "获取所有启用的知识库分类")
    @GetMapping("/categories")
    public Result<List<KnowledgeCategory>> getCategories() {
        try {
            log.info("获取知识库分类列表");
            List<KnowledgeCategory> categories = knowledgeService.getAllCategories();
            return Result.success(categories);
        } catch (Exception e) {
            log.error("获取分类列表失败", e);
            return Result.error("获取分类列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取顶级分类
     */
    @Operation(summary = "获取顶级分类", description = "获取所有顶级分类")
    @GetMapping("/categories/top")
    public Result<List<KnowledgeCategory>> getTopCategories() {
        try {
            log.info("获取顶级分类");
            List<KnowledgeCategory> categories = knowledgeService.getTopLevelCategories();
            return Result.success(categories);
        } catch (Exception e) {
            log.error("获取顶级分类失败", e);
            return Result.error("获取顶级分类失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取子分类
     */
    @Operation(summary = "获取子分类", description = "获取指定分类的子分类")
    @GetMapping("/categories/{parentId}/children")
    public Result<List<KnowledgeCategory>> getSubCategories(@PathVariable Long parentId) {
        try {
            log.info("获取子分类，父分类ID：{}", parentId);
            List<KnowledgeCategory> categories = knowledgeService.getSubCategories(parentId);
            return Result.success(categories);
        } catch (Exception e) {
            log.error("获取子分类失败", e);
            return Result.error("获取子分类失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取文章列表（分页）
     */
    @Operation(summary = "获取文章列表", description = "分页获取所有已发布文章")
    @GetMapping("/articles")
    public Result<List<KnowledgeArticle>> getArticles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            log.info("获取文章列表，页码：{}，每页数量：{}", page, pageSize);
            List<KnowledgeArticle> articles = knowledgeService.getAllArticles(page, pageSize);
            return Result.success(articles);
        } catch (Exception e) {
            log.error("获取文章列表失败", e);
            return Result.error("获取文章列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据分类获取文章
     */
    @Operation(summary = "根据分类获取文章", description = "获取指定分类下的文章列表")
    @GetMapping("/articles/category/{categoryId}")
    public Result<List<KnowledgeArticle>> getArticlesByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            log.info("根据分类获取文章，分类ID：{}，页码：{}", categoryId, page);
            List<KnowledgeArticle> articles = knowledgeService.getArticlesByCategory(categoryId, page, pageSize);
            return Result.success(articles);
        } catch (Exception e) {
            log.error("根据分类获取文章失败", e);
            return Result.error("根据分类获取文章失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取文章详情
     */
    @Operation(summary = "获取文章详情", description = "获取指定文章的详细内容")
    @GetMapping("/article/{id}")
    public Result<KnowledgeArticle> getArticle(@PathVariable Long id) {
        try {
            log.info("获取文章详情，文章ID：{}", id);
            KnowledgeArticle article = knowledgeService.getArticle(id);
            return Result.success(article);
        } catch (Exception e) {
            log.error("获取文章详情失败", e);
            return Result.error("获取文章详情失败：" + e.getMessage());
        }
    }
    
    /**
     * 搜索文章
     */
    @Operation(summary = "搜索文章", description = "根据关键词搜索文章")
    @GetMapping("/articles/search")
    public Result<List<KnowledgeArticle>> searchArticles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        try {
            log.info("搜索文章，关键词：{}，页码：{}", keyword, page);
            List<KnowledgeArticle> articles = knowledgeService.searchArticles(keyword, page, pageSize);
            return Result.success(articles);
        } catch (Exception e) {
            log.error("搜索文章失败", e);
            return Result.error("搜索文章失败：" + e.getMessage());
        }
    }
    
    /**
     * 点赞文章
     */
    @Operation(summary = "点赞文章", description = "为文章点赞")
    @PostMapping("/article/{id}/like")
    public Result<Void> likeArticle(@PathVariable Long id) {
        try {
            log.info("点赞文章，文章ID：{}", id);
            knowledgeService.likeArticle(id);
            return Result.success();
        } catch (Exception e) {
            log.error("点赞文章失败", e);
            return Result.error("点赞文章失败：" + e.getMessage());
        }
    }
    
    /**
     * 收藏文章
     */
    @Operation(summary = "收藏文章", description = "收藏指定文章")
    @PostMapping("/article/{id}/collect")
    public Result<Void> collectArticle(@PathVariable Long id) {
        try {
            log.info("收藏文章，文章ID：{}", id);
            knowledgeService.collectArticle(id);
            return Result.success();
        } catch (Exception e) {
            log.error("收藏文章失败", e);
            return Result.error("收藏文章失败：" + e.getMessage());
        }
    }
}
