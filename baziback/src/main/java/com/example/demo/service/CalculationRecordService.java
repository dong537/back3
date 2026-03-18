package com.example.demo.service;

import com.example.demo.entity.TbCalculationRecord;
import com.example.demo.mapper.CalculationRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 测算记录服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CalculationRecordService {

    private final CalculationRecordMapper calculationRecordMapper;
    private final AchievementService achievementService;

    /**
     * 保存测算记录
     */
    @Transactional
    public TbCalculationRecord saveRecord(TbCalculationRecord record) {
        log.info("保存测算记录: userId={}, recordType={}, recordTitle={}", 
                record.getUserId(), record.getRecordType(), record.getRecordTitle());
        
        calculationRecordMapper.insert(record);
        
        log.info("记录保存成功: id={}, userId={}", record.getId(), record.getUserId());
        
        // 保存记录后，异步检查占卜次数相关的成就（性能优化）
        if (record.getUserId() != null) {
            Long userId = record.getUserId();
            // 使用异步方式检查成就，不阻塞主流程
            // 注意：由于使用了@Async，需要在配置类中启用异步支持
            try {
                log.info("开始检查占卜成就: userId={}", userId);
                // 同步检查（如果异步有问题，可以改为异步）
                achievementService.checkDivinationAchievements(userId);
                log.info("占卜成就检查完成: userId={}", userId);
            } catch (Exception e) {
                log.error("保存记录后检查成就失败, userId={}", userId, e);
                // 不抛出异常，避免影响记录保存
            }
        } else {
            log.warn("记录保存成功但userId为null，跳过成就检查");
        }
        
        return record;
    }

    /**
     * 获取记录详情
     */
    public TbCalculationRecord getRecord(Long id) {
        return calculationRecordMapper.selectById(id);
    }

    /**
     * 获取用户所有记录
     */
    public List<TbCalculationRecord> getUserRecords(Long userId) {
        return calculationRecordMapper.selectByUserId(userId);
    }

    /**
     * 获取用户某类型的记录
     */
    public List<TbCalculationRecord> getUserRecordsByType(Long userId, String recordType) {
        return calculationRecordMapper.selectByUserIdAndType(userId, recordType);
    }

    /**
     * 分页获取用户记录
     */
    public List<TbCalculationRecord> getUserRecordsPaged(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        return calculationRecordMapper.selectByUserIdPaged(userId, offset, size);
    }

    /**
     * 更新记录
     */
    @Transactional
    public TbCalculationRecord updateRecord(TbCalculationRecord record) {
        calculationRecordMapper.update(record);
        return record;
    }

    /**
     * 删除记录
     */
    @Transactional
    public void deleteRecord(Long id) {
        calculationRecordMapper.delete(id);
    }

    /**
     * 删除用户所有记录
     */
    @Transactional
    public void deleteUserRecords(Long userId) {
        calculationRecordMapper.deleteByUserId(userId);
    }

    /**
     * 获取用户记录总数
     */
    public int getUserRecordCount(Long userId) {
        return calculationRecordMapper.countByUserId(userId);
    }

    /**
     * 获取用户某类型的记录总数
     */
    public int getUserRecordCountByType(Long userId, String recordType) {
        return calculationRecordMapper.countByUserIdAndType(userId, recordType);
    }
}
