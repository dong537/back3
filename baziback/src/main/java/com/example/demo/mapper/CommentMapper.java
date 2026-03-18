package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.entity.Comment;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    
    @Select("SELECT c.*, u.username, u.nickname, u.avatar, u.level " +
            "FROM tb_comment c LEFT JOIN tb_user u ON c.user_id = u.id " +
            "WHERE c.post_id = #{postId} AND c.parent_id = 0 AND c.status = 1 " +
            "ORDER BY c.created_at DESC")
    List<Comment> selectCommentsByPostId(IPage<Comment> page, @Param("postId") Long postId);
    
    @Select("SELECT COUNT(*) FROM tb_comment WHERE post_id = #{postId} AND parent_id = 0 AND status = 1")
    long countCommentsByPostId(@Param("postId") Long postId);
    
    @Select("SELECT c.*, u.username, u.nickname, u.avatar, u.level " +
            "FROM tb_comment c LEFT JOIN tb_user u ON c.user_id = u.id " +
            "WHERE c.parent_id = #{parentId} AND c.status = 1 " +
            "ORDER BY c.created_at ASC")
    List<Comment> selectRepliesByParentId(@Param("parentId") Long parentId);
    
    @Update("UPDATE tb_comment SET likes_count = likes_count + #{delta} WHERE id = #{commentId}")
    int updateLikesCount(@Param("commentId") Long commentId, @Param("delta") int delta);
}
