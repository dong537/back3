package com.example.demo.mapper;

import com.example.demo.entity.MembershipPackage;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 会员套餐Mapper接口
 */
@Mapper
public interface MembershipPackageMapper {

    /**
     * 根据ID查询套餐
     */
    @Select("SELECT * FROM tb_membership_package WHERE id = #{id}")
    MembershipPackage findById(Long id);

    /**
     * 查询所有上架的套餐
     */
    @Select("SELECT * FROM tb_membership_package WHERE status = 1 ORDER BY sort_order ASC")
    List<MembershipPackage> findAllActive();

    /**
     * 根据套餐类型查询
     */
    @Select("SELECT * FROM tb_membership_package WHERE package_type = #{packageType} AND status = 1")
    MembershipPackage findByPackageType(Integer packageType);
}
