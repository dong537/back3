package com.example.demo.service;

import com.example.demo.entity.TbContactRecord;
import com.example.demo.mapper.ContactRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactRecordService {

    private final ContactRecordMapper contactRecordMapper;

    /**
     * Contact telemetry is non-blocking. If the table has not been deployed yet,
     * we log and continue instead of breaking the user flow.
     */
    @Transactional
    public TbContactRecord saveRecord(TbContactRecord record) {
        try {
            contactRecordMapper.insert(record);
            log.info("Saved contact record: userId={}, contactType={}, actionType={}, sourcePage={}",
                    record.getUserId(), record.getContactType(), record.getActionType(), record.getSourcePage());
        } catch (DataAccessException ex) {
            log.warn("Skipped saving contact record due to database issue: userId={}, contactType={}, cause={}",
                    record.getUserId(), record.getContactType(), abbreviateException(ex));
        }
        return record;
    }

    public TbContactRecord getRecord(Long id) {
        try {
            return contactRecordMapper.selectById(id);
        } catch (DataAccessException ex) {
            log.warn("Failed to load contact record: id={}, cause={}", id, abbreviateException(ex));
            return null;
        }
    }

    public List<TbContactRecord> getUserRecords(Long userId) {
        try {
            return contactRecordMapper.selectByUserId(userId);
        } catch (DataAccessException ex) {
            log.warn("Failed to load contact records: userId={}, cause={}", userId, abbreviateException(ex));
            return Collections.emptyList();
        }
    }

    public List<TbContactRecord> getUserRecordsPaged(Long userId, int page, int size) {
        int offset = (page - 1) * size;
        try {
            return contactRecordMapper.selectByUserIdPaged(userId, offset, size);
        } catch (DataAccessException ex) {
            log.warn("Failed to load paged contact records: userId={}, cause={}", userId, abbreviateException(ex));
            return Collections.emptyList();
        }
    }

    public Map<String, Object> getUserContactStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        try {
            int total = contactRecordMapper.countByUserId(userId);
            int wechatCount = contactRecordMapper.countByUserIdAndContactType(userId, "wechat");
            stats.put("total", total);
            stats.put("wechat", wechatCount);
        } catch (DataAccessException ex) {
            log.warn("Failed to load contact record stats: userId={}, cause={}", userId, abbreviateException(ex));
            stats.put("total", 0);
            stats.put("wechat", 0);
        }
        return stats;
    }

    private String abbreviateException(DataAccessException ex) {
        Throwable cause = ex.getMostSpecificCause();
        String message = cause != null ? cause.getMessage() : ex.getMessage();
        if (message == null || message.isBlank()) {
            return ex.getClass().getSimpleName();
        }
        String normalized = message.replaceAll("\\s+", " ").trim();
        return normalized.length() > 180 ? normalized.substring(0, 180) + "..." : normalized;
    }
}
