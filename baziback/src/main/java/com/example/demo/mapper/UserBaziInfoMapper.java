package com.example.demo.mapper;

import com.example.demo.entity.UserBaziInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户八字信息Mapper
 */
@Mapper
public interface UserBaziInfoMapper {
    
    /**
     * 插入八字信息
     */
    @Insert("INSERT INTO tb_user_bazi_info (user_id, name, gender, birth_year, birth_month, birth_day, " +
            "birth_hour, birth_minute, is_lunar, timezone, birthplace, bazi_data, is_default) " +
            "VALUES (#{userId}, #{name}, #{gender}, #{birthYear}, #{birthMonth}, #{birthDay}, " +
            "#{birthHour}, #{birthMinute}, #{isLunar}, #{timezone}, #{birthplace}, #{baziData}, #{isDefault})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserBaziInfo baziInfo);
    
    /**
     * 更新八字信息
     */
    @Update("UPDATE tb_user_bazi_info SET name=#{name}, gender=#{gender}, birth_year=#{birthYear}, " +
            "birth_month=#{birthMonth}, birth_day=#{birthDay}, birth_hour=#{birthHour}, birth_minute=#{birthMinute}, " +
            "is_lunar=#{isLunar}, timezone=#{timezone}, birthplace=#{birthplace}, bazi_data=#{baziData}, " +
            "is_default=#{isDefault} WHERE id=#{id}")
    int update(UserBaziInfo baziInfo);
    
    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM tb_user_bazi_info WHERE id=#{id}")
    UserBaziInfo findById(Long id);
    
    /**
     * 根据用户ID查询所有八字信息
     */
    @Select("SELECT * FROM tb_user_bazi_info WHERE user_id=#{userId} ORDER BY is_default DESC, create_time DESC")
    List<UserBaziInfo> findByUserId(Long userId);
    
    /**
     * 根据用户ID查询默认八字信息
     */
    @Select("SELECT * FROM tb_user_bazi_info WHERE user_id=#{userId} AND is_default=1 LIMIT 1")
    UserBaziInfo findDefaultByUserId(Long userId);
    
    /**
     * 取消用户的所有默认八字
     */
    @Update("UPDATE tb_user_bazi_info SET is_default=0 WHERE user_id=#{userId}")
    int clearDefaultByUserId(Long userId);
    
    /**
     * 删除八字信息
     */
    @Delete("DELETE FROM tb_user_bazi_info WHERE id=#{id}")
    int deleteById(Long id);
    
    /**
     * 统计用户的八字信息数量
     */
    @Select("SELECT COUNT(*) FROM tb_user_bazi_info WHERE user_id=#{userId}")
    int countByUserId(Long userId);
}
