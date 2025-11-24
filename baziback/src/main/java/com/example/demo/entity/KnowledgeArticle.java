package com.example.demo.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 知识库文章实体
 */
@Data
public class KnowledgeArticle {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 文章标题
     */
    private String title;
    
    /**
     * 副标题
     */
    private String subtitle;
    
    /**
     * 作者
     */
    private String author;
    
    /**
     * 封面图片
     */
    private String coverImage;
    
    /**
     * 摘要
     */
    private String summary;
    
    /**
     * 文章内容（Markdown格式）
     */
    private String content;
    
    /**
     * 标签（JSON数组）
     */
    private String tags;
    
    /**
     * 浏览次数
     */
    private Integer viewCount;
    
    /**
     * 点赞次数
     */
    private Integer likeCount;
    
    /**
     * 收藏次数
     */
    private Integer collectCount;
    
    /**
     * 排序
     */
    private Integer sortOrder;
    
    /**
     * 状态：0-草稿，1-已发布，2-已下架
     */
    private Integer status;
    
    /**
     * 发布时间
     */
    private LocalDateTime publishTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
