package com.example.demo.service;

import com.example.demo.entity.KnowledgeArticle;
import com.example.demo.entity.KnowledgeCategory;
import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.KnowledgeArticleMapper;
import com.example.demo.mapper.KnowledgeCategoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 知识库服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {
    
    private final KnowledgeCategoryMapper categoryMapper;
    private final KnowledgeArticleMapper articleMapper;
    
    // ========== 分类管理 ==========
    
    /**
     * 创建分类
     */
    @Transactional
    public KnowledgeCategory createCategory(KnowledgeCategory category) {
        // 检查编码是否已存在
        KnowledgeCategory existing = categoryMapper.findByCode(category.getCategoryCode());
        if (existing != null) {
            throw new BusinessException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        }
        
        int result = categoryMapper.insert(category);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "创建分类失败");
        }
        
        log.info("创建知识库分类成功，分类ID：{}", category.getId());
        return category;
    }
    
    /**
     * 更新分类
     */
    @Transactional
    public KnowledgeCategory updateCategory(KnowledgeCategory category) {
        KnowledgeCategory existing = categoryMapper.findById(category.getId());
        if (existing == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        
        int result = categoryMapper.update(category);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "更新分类失败");
        }
        
        log.info("更新知识库分类成功，分类ID：{}", category.getId());
        return categoryMapper.findById(category.getId());
    }
    
    /**
     * 获取所有启用的分类
     */
    public List<KnowledgeCategory> getAllCategories() {
        return categoryMapper.findAllEnabled();
    }
    
    /**
     * 获取顶级分类
     */
    public List<KnowledgeCategory> getTopLevelCategories() {
        return categoryMapper.findTopLevel();
    }
    
    /**
     * 获取子分类
     */
    public List<KnowledgeCategory> getSubCategories(Long parentId) {
        return categoryMapper.findByParentId(parentId);
    }
    
    /**
     * 删除分类
     */
    @Transactional
    public void deleteCategory(Long categoryId) {
        KnowledgeCategory category = categoryMapper.findById(categoryId);
        if (category == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        
        // 检查是否有子分类
        int childCount = categoryMapper.countChildren(categoryId);
        if (childCount > 0) {
            throw new BusinessException(ErrorCode.CATEGORY_HAS_CHILDREN);
        }
        
        // 检查是否有文章
        int articleCount = articleMapper.countByCategoryId(categoryId);
        if (articleCount > 0) {
            throw new BusinessException(ErrorCode.CATEGORY_HAS_ARTICLES);
        }
        
        int result = categoryMapper.deleteById(categoryId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "删除分类失败");
        }
        
        log.info("删除知识库分类成功，分类ID：{}", categoryId);
    }
    
    // ========== 文章管理 ==========
    
    /**
     * 创建文章
     */
    @Transactional
    public KnowledgeArticle createArticle(KnowledgeArticle article) {
        // 验证分类是否存在
        KnowledgeCategory category = categoryMapper.findById(article.getCategoryId());
        if (category == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        
        if (article.getViewCount() == null) {
            article.setViewCount(0);
        }
        if (article.getLikeCount() == null) {
            article.setLikeCount(0);
        }
        if (article.getCollectCount() == null) {
            article.setCollectCount(0);
        }
        
        int result = articleMapper.insert(article);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "创建文章失败");
        }
        
        log.info("创建知识库文章成功，文章ID：{}", article.getId());
        return article;
    }
    
    /**
     * 更新文章
     */
    @Transactional
    public KnowledgeArticle updateArticle(KnowledgeArticle article) {
        KnowledgeArticle existing = articleMapper.findById(article.getId());
        if (existing == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        
        int result = articleMapper.update(article);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "更新文章失败");
        }
        
        log.info("更新知识库文章成功，文章ID：{}", article.getId());
        return articleMapper.findById(article.getId());
    }
    
    /**
     * 获取文章详情
     */
    @Transactional
    public KnowledgeArticle getArticle(Long id) {
        KnowledgeArticle article = articleMapper.findById(id);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        
        if (article.getStatus() != 1) {
            throw new BusinessException(ErrorCode.ARTICLE_DISABLED);
        }
        
        // 增加浏览次数
        articleMapper.incrementViewCount(id);
        
        return article;
    }
    
    /**
     * 根据分类获取文章列表（分页）
     */
    public List<KnowledgeArticle> getArticlesByCategory(Long categoryId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return articleMapper.findByCategoryId(categoryId, offset, pageSize);
    }
    
    /**
     * 获取所有已发布文章（分页）
     */
    public List<KnowledgeArticle> getAllArticles(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return articleMapper.findAllPublished(offset, pageSize);
    }
    
    /**
     * 搜索文章
     */
    public List<KnowledgeArticle> searchArticles(String keyword, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return articleMapper.search(keyword, offset, pageSize);
    }
    
    /**
     * 点赞文章
     */
    @Transactional
    public void likeArticle(Long articleId) {
        KnowledgeArticle article = articleMapper.findById(articleId);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        
        articleMapper.incrementLikeCount(articleId);
        log.info("点赞文章成功，文章ID：{}", articleId);
    }
    
    /**
     * 收藏文章
     */
    @Transactional
    public void collectArticle(Long articleId) {
        KnowledgeArticle article = articleMapper.findById(articleId);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        
        articleMapper.incrementCollectCount(articleId);
        log.info("收藏文章成功，文章ID：{}", articleId);
    }
    
    /**
     * 取消收藏文章
     */
    @Transactional
    public void uncollectArticle(Long articleId) {
        KnowledgeArticle article = articleMapper.findById(articleId);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        
        articleMapper.decrementCollectCount(articleId);
        log.info("取消收藏文章成功，文章ID：{}", articleId);
    }
    
    /**
     * 删除文章
     */
    @Transactional
    public void deleteArticle(Long articleId) {
        KnowledgeArticle article = articleMapper.findById(articleId);
        if (article == null) {
            throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
        }
        
        int result = articleMapper.deleteById(articleId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "删除文章失败");
        }
        
        log.info("删除知识库文章成功，文章ID：{}", articleId);
    }
}
