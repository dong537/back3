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
    @Insert("INSERT INTO tb_user (username, password, email, phone, nickname, status) " +
            "VALUES (#{username}, #{password}, #{email}, #{phone}, #{nickname}, #{status})")
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
    
    /**
     * 根据OAuth提供商和OAuth用户ID查询用户
     */
    @Select("SELECT * FROM tb_user WHERE oauth_provider = #{oauthProvider} AND oauth_id = #{oauthId}")
    User findByOauthId(@Param("oauthProvider") String oauthProvider, @Param("oauthId") String oauthId);

    /**
     * 插入OAuth用户（无密码）
     */
    @Insert("INSERT INTO tb_user (username, password, email, nickname, avatar, status, oauth_provider, oauth_id) " +
            "VALUES (#{username}, #{password}, #{email}, #{nickname}, #{avatar}, #{status}, #{oauthProvider}, #{oauthId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertOauthUser(User user);

    /**
     * 更新OAuth用户信息（昵称、头像）
     */
    @Update("UPDATE tb_user SET nickname = #{nickname}, avatar = #{avatar}, last_login_time = #{lastLoginTime}, last_login_ip = #{lastLoginIp} WHERE id = #{id}")
    int updateOauthUserInfo(User user);

    /**
     * 更新用户积分（同时更新current_points和total_points）
     */
    @Update("UPDATE tb_user SET " +
            "current_points = current_points + #{points}, " +
            "total_points = total_points + CASE WHEN #{points} > 0 THEN #{points} ELSE 0 END " +
            "WHERE id = #{userId}")
    int updatePoints(@Param("userId") Long userId, @Param("points") Integer points);
}
