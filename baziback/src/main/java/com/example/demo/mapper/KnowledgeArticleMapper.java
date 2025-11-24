package com.example.demo.mapper;

import com.example.demo.entity.KnowledgeArticle;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 知识库文章Mapper
 */
@Mapper
public interface KnowledgeArticleMapper {
    
    /**
     * 插入文章
     */
    @Insert("INSERT INTO tb_knowledge_article (category_id, title, subtitle, author, cover_image, " +
            "summary, content, tags, view_count, like_count, collect_count, sort_order, status, publish_time) " +
            "VALUES (#{categoryId}, #{title}, #{subtitle}, #{author}, #{coverImage}, #{summary}, " +
            "#{content}, #{tags}, #{viewCount}, #{likeCount}, #{collectCount}, #{sortOrder}, #{status}, #{publishTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(KnowledgeArticle article);
    
    /**
     * 更新文章
     */
    @Update("UPDATE tb_knowledge_article SET category_id=#{categoryId}, title=#{title}, subtitle=#{subtitle}, " +
            "author=#{author}, cover_image=#{coverImage}, summary=#{summary}, content=#{content}, tags=#{tags}, " +
            "sort_order=#{sortOrder}, status=#{status}, publish_time=#{publishTime} WHERE id=#{id}")
    int update(KnowledgeArticle article);
    
    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM tb_knowledge_article WHERE id=#{id}")
    KnowledgeArticle findById(Long id);
    
    /**
     * 根据分类ID查询文章列表
     */
    @Select("SELECT * FROM tb_knowledge_article WHERE category_id=#{categoryId} AND status=1 " +
            "ORDER BY sort_order ASC, publish_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<KnowledgeArticle> findByCategoryId(@Param("categoryId") Long categoryId, 
                                             @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 查询所有已发布文章
     */
    @Select("SELECT * FROM tb_knowledge_article WHERE status=1 ORDER BY publish_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<KnowledgeArticle> findAllPublished(@Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 搜索文章（标题和内容）
     */
    @Select("SELECT * FROM tb_knowledge_article WHERE status=1 AND (title LIKE CONCAT('%', #{keyword}, '%') " +
            "OR content LIKE CONCAT('%', #{keyword}, '%')) ORDER BY publish_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<KnowledgeArticle> search(@Param("keyword") String keyword, @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 增加浏览次数
     */
    @Update("UPDATE tb_knowledge_article SET view_count=view_count+1 WHERE id=#{id}")
    int incrementViewCount(Long id);
    
    /**
     * 增加点赞次数
     */
    @Update("UPDATE tb_knowledge_article SET like_count=like_count+1 WHERE id=#{id}")
    int incrementLikeCount(Long id);
    
    /**
     * 增加收藏次数
     */
    @Update("UPDATE tb_knowledge_article SET collect_count=collect_count+1 WHERE id=#{id}")
    int incrementCollectCount(Long id);
    
    /**
     * 减少收藏次数
     */
    @Update("UPDATE tb_knowledge_article SET collect_count=collect_count-1 WHERE id=#{id} AND collect_count>0")
    int decrementCollectCount(Long id);
    
    /**
     * 统计分类下的文章数量
     */
    @Select("SELECT COUNT(*) FROM tb_knowledge_article WHERE category_id=#{categoryId} AND status=1")
    int countByCategoryId(Long categoryId);
    
    /**
     * 删除文章
     */
    @Delete("DELETE FROM tb_knowledge_article WHERE id=#{id}")
    int deleteById(Long id);
}
