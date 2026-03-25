package com.example.demo.mapper;

import com.example.demo.entity.TbUserFavorite;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FavoriteMapper {

    // ===================== 社区：帖子收藏（tb_favorite） =====================

    @Select("SELECT post_id FROM tb_favorite WHERE user_id = #{userId}")
    List<Long> selectFavoritePostIds(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM tb_favorite WHERE user_id = #{userId} AND post_id = #{postId}")
    int exists(@Param("userId") Long userId, @Param("postId") Long postId);

    @Delete("DELETE FROM tb_favorite WHERE user_id = #{userId} AND post_id = #{postId}")
    int delete(@Param("userId") Long userId, @Param("postId") Long postId);

    @Insert("INSERT INTO tb_favorite (user_id, post_id) VALUES (#{userId}, #{postId})")
    int insert(@Param("userId") Long userId, @Param("postId") Long postId);

    // ===================== 用户：多类型收藏（tb_user_favorite） =====================

    @Select("SELECT * FROM tb_user_favorite WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<TbUserFavorite> findByUserId(Long userId);

    @Select("SELECT " +
            "(SELECT COUNT(*) FROM tb_user_favorite WHERE user_id = #{userId}) + " +
            "(SELECT COUNT(*) FROM tb_favorite WHERE user_id = #{userId})")
    Integer countAllFavoritesByUserId(Long userId);

    @Insert("INSERT INTO tb_user_favorite (user_id, favorite_type, data_id, title, summary, data, create_time) " +
            "VALUES (#{userId}, #{favoriteType}, #{dataId}, #{title}, #{summary}, #{data}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TbUserFavorite favorite);

    @Delete("DELETE FROM tb_user_favorite WHERE id = #{id} AND user_id = #{userId}")
    int deleteById(@Param("id") Long id, @Param("userId") Long userId);

    @DeleteProvider(type = FavoriteSqlBuilder.class, method = "buildDeleteByIds")
    int deleteByIds(@Param("ids") List<Long> ids, @Param("userId") Long userId);

    @Delete("DELETE FROM tb_user_favorite WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);

    // Using a provider class for dynamic SQL
    class FavoriteSqlBuilder {
        public String buildDeleteByIds(@Param("ids") List<Long> ids, @Param("userId") Long userId) {
            StringBuilder sb = new StringBuilder();
            sb.append("DELETE FROM tb_user_favorite WHERE user_id = #{userId} AND id IN (");
            for (int i = 0; i < ids.size(); i++) {
                sb.append("#{ids[").append(i).append("]}");
                if (i < ids.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append(")");
            return sb.toString();
        }
    }
}
