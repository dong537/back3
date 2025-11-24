package com.example.demo.service;

import com.example.demo.entity.UserBaziInfo;
import com.example.demo.enums.ErrorCode;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.UserBaziInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户八字信息服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserBaziInfoService {
    
    private final UserBaziInfoMapper baziInfoMapper;
    
    /**
     * 创建八字信息
     */
    @Transactional
    public UserBaziInfo createBaziInfo(UserBaziInfo baziInfo) {
        // 如果设置为默认，先取消其他默认八字
        if (baziInfo.getIsDefault() != null && baziInfo.getIsDefault() == 1) {
            baziInfoMapper.clearDefaultByUserId(baziInfo.getUserId());
        }
        
        int result = baziInfoMapper.insert(baziInfo);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "创建八字信息失败");
        }
        
        log.info("创建八字信息成功，用户ID：{}，八字ID：{}", baziInfo.getUserId(), baziInfo.getId());
        return baziInfo;
    }
    
    /**
     * 更新八字信息
     */
    @Transactional
    public UserBaziInfo updateBaziInfo(UserBaziInfo baziInfo) {
        UserBaziInfo existing = baziInfoMapper.findById(baziInfo.getId());
        if (existing == null) {
            throw new BusinessException(ErrorCode.BAZI_INFO_NOT_FOUND);
        }
        
        // 如果设置为默认，先取消其他默认八字
        if (baziInfo.getIsDefault() != null && baziInfo.getIsDefault() == 1) {
            baziInfoMapper.clearDefaultByUserId(baziInfo.getUserId());
        }
        
        int result = baziInfoMapper.update(baziInfo);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "更新八字信息失败");
        }
        
        log.info("更新八字信息成功，八字ID：{}", baziInfo.getId());
        return baziInfoMapper.findById(baziInfo.getId());
    }
    
    /**
     * 获取八字信息
     */
    public UserBaziInfo getBaziInfo(Long id) {
        UserBaziInfo baziInfo = baziInfoMapper.findById(id);
        if (baziInfo == null) {
            throw new BusinessException(ErrorCode.BAZI_INFO_NOT_FOUND);
        }
        return baziInfo;
    }
    
    /**
     * 获取用户的所有八字信息
     */
    public List<UserBaziInfo> getUserBaziInfoList(Long userId) {
        return baziInfoMapper.findByUserId(userId);
    }
    
    /**
     * 获取用户的默认八字信息
     */
    public UserBaziInfo getDefaultBaziInfo(Long userId) {
        UserBaziInfo baziInfo = baziInfoMapper.findDefaultByUserId(userId);
        if (baziInfo == null) {
            // 如果没有默认八字，返回第一个
            List<UserBaziInfo> list = baziInfoMapper.findByUserId(userId);
            if (!list.isEmpty()) {
                return list.get(0);
            }
        }
        return baziInfo;
    }
    
    /**
     * 设置默认八字
     */
    @Transactional
    public void setDefaultBaziInfo(Long userId, Long baziInfoId) {
        UserBaziInfo baziInfo = baziInfoMapper.findById(baziInfoId);
        if (baziInfo == null) {
            throw new BusinessException(ErrorCode.BAZI_INFO_NOT_FOUND);
        }
        
        if (!baziInfo.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        
        // 取消其他默认八字
        baziInfoMapper.clearDefaultByUserId(userId);
        
        // 设置新的默认八字
        baziInfo.setIsDefault(1);
        baziInfoMapper.update(baziInfo);
        
        log.info("设置默认八字成功，用户ID：{}，八字ID：{}", userId, baziInfoId);
    }
    
    /**
     * 删除八字信息
     */
    @Transactional
    public void deleteBaziInfo(Long userId, Long baziInfoId) {
        UserBaziInfo baziInfo = baziInfoMapper.findById(baziInfoId);
        if (baziInfo == null) {
            throw new BusinessException(ErrorCode.BAZI_INFO_NOT_FOUND);
        }
        
        if (!baziInfo.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
        
        int result = baziInfoMapper.deleteById(baziInfoId);
        if (result <= 0) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "删除八字信息失败");
        }
        
        log.info("删除八字信息成功，用户ID：{}，八字ID：{}", userId, baziInfoId);
    }
}
