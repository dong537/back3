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
     * 更新用户信息
     */
    @Update("UPDATE tb_user SET username = #{username}, email = #{email}, phone = #{phone}, " +
            "nickname = #{nickname}, avatar = #{avatar}, status = #{status}, " +
            "last_login_time = #{lastLoginTime}, last_login_ip = #{lastLoginIp}, " +
            "update_time = #{updateTime} WHERE id = #{id}")
    int updateUser(User user);
}
