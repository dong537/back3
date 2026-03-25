package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.entity.Post;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PostMapper extends BaseMapper<Post> {
    
    @Select("SELECT p.*, u.username, u.nickname, u.avatar, u.level " +
            "FROM tb_post p LEFT JOIN tb_user u ON p.user_id = u.id " +
            "WHERE p.status = 1 " +
            "ORDER BY p.is_top DESC, p.created_at DESC")
    List<Post> selectPostsWithUser(IPage<Post> page);
    
    @Select("SELECT p.*, u.username, u.nickname, u.avatar, u.level " +
            "FROM tb_post p LEFT JOIN tb_user u ON p.user_id = u.id " +
            "WHERE p.status = 1 AND p.category = #{category} " +
            "ORDER BY p.is_top DESC, p.created_at DESC")
    List<Post> selectPostsByCategory(IPage<Post> page, @Param("category") String category);
    
    @Select("SELECT p.*, u.username, u.nickname, u.avatar, u.level " +
            "FROM tb_post p LEFT JOIN tb_user u ON p.user_id = u.id " +
            "WHERE p.status = 1 AND p.user_id = #{userId} " +
            "ORDER BY p.created_at DESC")
    List<Post> selectPostsByUserId(IPage<Post> page, @Param("userId") Long userId);
    
    @Select("SELECT p.*, u.username, u.nickname, u.avatar, u.level " +
            "FROM tb_post p LEFT JOIN tb_user u ON p.user_id = u.id " +
            "WHERE p.status = 1 AND p.is_hot = 1 " +
            "ORDER BY p.likes_count DESC, p.created_at DESC")
    List<Post> selectHotPosts(IPage<Post> page);
    
    @Select("SELECT COUNT(*) FROM tb_post WHERE status = 1")
    long countAllPosts();
    
    @Select("SELECT COUNT(*) FROM tb_post WHERE status = 1 AND category = #{category}")
    long countPostsByCategory(@Param("category") String category);
    
    @Select("SELECT COUNT(*) FROM tb_post WHERE status = 1 AND user_id = #{userId}")
    long countPostsByUserId(@Param("userId") Long userId);
    
    @Select("SELECT COUNT(*) FROM tb_post WHERE status = 1 AND is_hot = 1")
    long countHotPosts();
    
    @Update("UPDATE tb_post SET likes_count = likes_count + #{delta} WHERE id = #{postId}")
    int updateLikesCount(@Param("postId") Long postId, @Param("delta") int delta);
    
    @Update("UPDATE tb_post SET comments_count = comments_count + #{delta} WHERE id = #{postId}")
    int updateCommentsCount(@Param("postId") Long postId, @Param("delta") int delta);
    
    @Update("UPDATE tb_post SET shares_count = shares_count + 1 WHERE id = #{postId}")
    int incrementSharesCount(@Param("postId") Long postId);
    
    @Update("UPDATE tb_post SET views_count = views_count + 1 WHERE id = #{postId}")
    int incrementViewsCount(@Param("postId") Long postId);
}
