package com.example.demo.mapper;

import com.example.demo.entity.TbContactRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 联系方式记录 Mapper
 */
@Mapper
public interface ContactRecordMapper {

    /**
     * 插入联系记录
     */
    @Insert("INSERT INTO tb_contact_record " +
            "(user_id, contact_type, contact_name, contact_info, source_page, source_type, " +
            "related_record_id, action_type, ip_address, user_agent, create_time, update_time) " +
            "VALUES (#{userId}, #{contactType}, #{contactName}, #{contactInfo}, #{sourcePage}, #{sourceType}, " +
            "#{relatedRecordId}, #{actionType}, #{ipAddress}, #{userAgent}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TbContactRecord record);

    /**
     * 根据ID查询
     */
    @Select("SELECT * FROM tb_contact_record WHERE id = #{id}")
    TbContactRecord selectById(Long id);

    /**
     * 根据用户ID查询所有记录
     */
    @Select("SELECT * FROM tb_contact_record WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<TbContactRecord> selectByUserId(Long userId);

    /**
     * 分页查询用户记录
     */
    @Select("SELECT * FROM tb_contact_record WHERE user_id = #{userId} " +
            "ORDER BY create_time DESC LIMIT #{offset}, #{limit}")
    List<TbContactRecord> selectByUserIdPaged(Long userId, int offset, int limit);

    /**
     * 统计用户联系记录数
     */
    @Select("SELECT COUNT(*) FROM tb_contact_record WHERE user_id = #{userId}")
    int countByUserId(Long userId);

    /**
     * 根据联系方式类型统计
     */
    @Select("SELECT COUNT(*) FROM tb_contact_record " +
            "WHERE user_id = #{userId} AND contact_type = #{contactType}")
    int countByUserIdAndContactType(Long userId, String contactType);

    /**
     * 根据来源页面统计
     */
    @Select("SELECT COUNT(*) FROM tb_contact_record " +
            "WHERE user_id = #{userId} AND source_page = #{sourcePage}")
    int countByUserIdAndSourcePage(Long userId, String sourcePage);
}
