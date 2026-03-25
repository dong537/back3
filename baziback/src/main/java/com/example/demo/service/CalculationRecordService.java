package com.example.demo.service;

import com.example.demo.entity.TbCalculationRecord;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.CalculationRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculationRecordService {

    private static final int MAX_PAGE_SIZE = 50;

    private final CalculationRecordMapper calculationRecordMapper;
    private final AchievementService achievementService;
    private final ReferralService referralService;

    @Transactional
    public TbCalculationRecord saveRecord(TbCalculationRecord record) {
        normalizeRecord(record);

        log.info("Saving calculation record: userId={}, recordType={}, recordTitle={}",
                record.getUserId(), record.getRecordType(), record.getRecordTitle());

        calculationRecordMapper.insert(record);

        if (record.getUserId() != null) {
            try {
                achievementService.checkDivinationAchievements(record.getUserId());
            } catch (Exception e) {
                log.error("Failed to check achievements after saving record, userId={}", record.getUserId(), e);
            }
            try {
                referralService.recordFirstDivination(record.getUserId());
            } catch (Exception e) {
                log.error("Failed to record referral first-divination after saving record, userId={}", record.getUserId(), e);
            }
        }

        return record;
    }

    public TbCalculationRecord getUserRecord(Long userId, Long id) {
        if (userId == null) {
            throw new BusinessException("用户未登录", HttpStatus.UNAUTHORIZED);
        }
        TbCalculationRecord record = calculationRecordMapper.selectByIdAndUserId(id, userId);
        if (record == null) {
            throw new BusinessException("记录不存在", HttpStatus.NOT_FOUND);
        }
        return record;
    }

    public List<TbCalculationRecord> getUserRecords(Long userId) {
        return calculationRecordMapper.selectByUserId(userId);
    }

    public List<TbCalculationRecord> getUserRecordsByType(Long userId, String recordType) {
        return calculationRecordMapper.selectByUserIdAndType(userId, normalizeRecordType(recordType));
    }

    public List<TbCalculationRecord> getUserRecordsPaged(Long userId, String recordType, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int offset = (safePage - 1) * safeSize;

        if (recordType == null || recordType.isBlank()) {
            return calculationRecordMapper.selectByUserIdPaged(userId, offset, safeSize);
        }
        return calculationRecordMapper.selectByUserIdAndTypePaged(
                userId,
                normalizeRecordType(recordType),
                offset,
                safeSize
        );
    }

    @Transactional
    public TbCalculationRecord updateUserRecord(Long userId, Long id, TbCalculationRecord record) {
        TbCalculationRecord existing = getUserRecord(userId, id);
        TbCalculationRecord updatedRecord = TbCalculationRecord.builder()
                .id(existing.getId())
                .userId(userId)
                .recordType(existing.getRecordType())
                .recordTitle(trimToDefault(record.getRecordTitle(), existing.getRecordTitle()))
                .question(trimToNull(record.getQuestion()))
                .summary(trimToNull(record.getSummary()))
                .inputData(trimToDefault(record.getInputData(), existing.getInputData()))
                .data(trimToDefault(record.getData(), existing.getData()))
                .build();

        int updated = calculationRecordMapper.updateByUserId(updatedRecord);
        if (updated <= 0) {
            throw new BusinessException("更新记录失败");
        }
        return getUserRecord(userId, id);
    }

    @Transactional
    public void deleteUserRecord(Long userId, Long id) {
        int deleted = calculationRecordMapper.deleteByIdAndUserId(id, userId);
        if (deleted <= 0) {
            throw new BusinessException("记录不存在", HttpStatus.NOT_FOUND);
        }
    }

    @Transactional
    public void deleteUserRecords(Long userId) {
        calculationRecordMapper.deleteByUserId(userId);
    }

    public int getUserRecordCount(Long userId) {
        return calculationRecordMapper.countByUserId(userId);
    }

    public int getUserRecordCountByType(Long userId, String recordType) {
        return calculationRecordMapper.countByUserIdAndType(userId, normalizeRecordType(recordType));
    }

    private void normalizeRecord(TbCalculationRecord record) {
        if (record == null) {
            throw new BusinessException("记录内容不能为空");
        }
        record.setRecordType(normalizeRecordType(record.getRecordType()));
        record.setRecordTitle(trimToDefault(record.getRecordTitle(), "未命名记录"));
        record.setQuestion(trimToNull(record.getQuestion()));
        record.setSummary(trimToNull(record.getSummary()));
        record.setInputData(trimToNull(record.getInputData()));
        record.setData(trimToDefault(record.getData(), "{}"));
    }

    private String normalizeRecordType(String recordType) {
        String normalized = trimToNull(recordType);
        if (normalized == null) {
            throw new BusinessException("记录类型不能为空");
        }
        return normalized.toLowerCase();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String trimToDefault(String value, String defaultValue) {
        String trimmed = trimToNull(value);
        return trimmed != null ? trimmed : defaultValue;
    }
}
