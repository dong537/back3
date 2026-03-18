package com.example.demo.service;

import com.example.demo.entity.TbContactRecord;
import com.example.demo.mapper.ContactRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 联系方式记录服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContactRecordService {

    private final ContactRecordMapper contactRecordMapper;

    /**
     * 保存联系记录
     */
    @Transactional
    public TbContactRecord saveRecord(TbContactRecord record) {
        contactRecordMapper.insert(record);
        log.info("保存联系记录: userId={}, contactType={}, actionType={}, sourcePage={}",
                record.getUserId(), record.getContactType(), record.getActionType(), record.getSourcePage());
        return record;
    }

    /**
     * 获取记录详情
     */
    public TbContactRecord getRecord(Long id) {
        return contactRecordMapper.selectById(id);
    }

    /**
     * 获取用户所有联系记录
     */
    public List<TbContactRecord> getUserRecords(Long userId) {
        return contactRecordMapper.selectByUserId(userId);
    }

    /**
     * 分页获取用户记录
     */
    public List<TbContactRecord> getUserRecordsPaged(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        return contactRecordMapper.selectByUserIdPaged(userId, offset, size);
    }

    /**
     * 获取用户联系记录统计
     */
    public Map<String, Object> getUserContactStats(Long userId) {
        int total = contactRecordMapper.countByUserId(userId);
        int wechatCount = contactRecordMapper.countByUserIdAndContactType(userId, "wechat");
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("wechat", wechatCount);
        
        return stats;
    }
}
