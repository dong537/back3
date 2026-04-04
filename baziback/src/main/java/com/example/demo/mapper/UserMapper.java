package com.example.demo.mapper;

import com.example.demo.entity.User;
import org.apache.ibatis.annotations.*;

/**
 * 用户数据访问层
 */
@Mapper
public interface UserMapper {
    
    /**
     * 根据用户名查询用户
     */
    @Select("SELECT * FROM tb_user WHERE username = #{username}")
    User findByUsername(String username);
    
    /**
     * 根据邮箱查询用户
     */
    @Select("SELECT * FROM tb_user WHERE email = #{email}")
    User findByEmail(String email);
    
    /**
     * 根据手机号查询用户
     */
    @Select("SELECT * FROM tb_user WHERE phone = #{phone}")
    User findByPhone(String phone);
    
    /**
     * 插入新用户
     */
    @Insert("INSERT INTO tb_user (username, password, email, phone, nickname, avatar, status) " +
            "VALUES (#{username}, #{password}, #{email}, #{phone}, #{nickname}, #{avatar}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);
    
    /**
     * 更新最后登录时间和IP
     */
    @Update("UPDATE tb_user SET last_login_time = #{lastLoginTime}, last_login_ip = #{lastLoginIp} " +
            "WHERE id = #{id}")
    int updateLastLogin(User user);
    
    /**
     * 根据ID查询用户
     */
    @Select("SELECT * FROM tb_user WHERE id = #{id}")
    User findById(Long id);

    @Update("""
            <script>
            UPDATE tb_user
            <set>
                <if test="email != null and email != ''">email = #{email},</if>
                <if test="nickname != null and nickname != ''">nickname = #{nickname},</if>
                <if test="avatar != null and avatar != ''">avatar = #{avatar},</if>
                update_time = NOW()
            </set>
            WHERE id = #{id}
            </script>
            """)
    int updateProfile(User user);
    
    /**
     * 更新用户积分（同时更新current_points和total_points）
     */
    @Update("UPDATE tb_user SET " +
            "current_points = current_points + #{points}, " +
            "total_points = total_points + CASE WHEN #{points} > 0 THEN #{points} ELSE 0 END " +
            "WHERE id = #{userId}")
    int updatePoints(@Param("userId") Long userId, @Param("points") Integer points);
}
