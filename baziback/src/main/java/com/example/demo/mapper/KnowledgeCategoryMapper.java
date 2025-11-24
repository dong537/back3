package com.example.demo.mapper;

import com.example.demo.entity.KnowledgeCategory;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 知识库分类Mapper
 */
@Mapper
public interface KnowledgeCategoryMapper {
    
    /**
     * 插入分类
     */
    @Insert("INSERT INTO tb_knowledge_category (parent_id, category_name, category_code, description, " +
            "icon, sort_order, status) VALUES (#{parentId}, #{categoryName}, #{categoryCode}, " +
            "#{description}, #{icon}, #{sortOrder}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(KnowledgeCategory category);
    
    /**
     * 更新分类
     */
    @Update("UPDATE tb_knowledge_category SET category_name=#{categoryName}, description=#{description}, " +
            "icon=#{icon}, sort_order=#{sortOrder}, status=#{status} WHERE id=#{id}")
    int update(KnowledgeCategory category);
    
    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM tb_knowledge_category WHERE id=#{id}")
    KnowledgeCategory findById(Long id);
    
    /**
     * 根据编码查询
     */
    @Select("SELECT * FROM tb_knowledge_category WHERE category_code=#{categoryCode}")
    KnowledgeCategory findByCode(String categoryCode);
    
    /**
     * 查询所有启用的分类
     */
    @Select("SELECT * FROM tb_knowledge_category WHERE status=1 ORDER BY sort_order ASC, id ASC")
    List<KnowledgeCategory> findAllEnabled();
    
    /**
     * 根据父ID查询子分类
     */
    @Select("SELECT * FROM tb_knowledge_category WHERE parent_id=#{parentId} AND status=1 " +
            "ORDER BY sort_order ASC, id ASC")
    List<KnowledgeCategory> findByParentId(Long parentId);
    
    /**
     * 查询顶级分类
     */
    @Select("SELECT * FROM tb_knowledge_category WHERE parent_id=0 AND status=1 ORDER BY sort_order ASC, id ASC")
    List<KnowledgeCategory> findTopLevel();
    
    /**
     * 统计子分类数量
     */
    @Select("SELECT COUNT(*) FROM tb_knowledge_category WHERE parent_id=#{parentId}")
    int countChildren(Long parentId);
    
    /**
     * 删除分类
     */
    @Delete("DELETE FROM tb_knowledge_category WHERE id=#{id}")
    int deleteById(Long id);
}
