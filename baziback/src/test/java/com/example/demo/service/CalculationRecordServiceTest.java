package com.example.demo.service;

import com.example.demo.entity.TbCalculationRecord;
import com.example.demo.exception.BusinessException;
import com.example.demo.mapper.CalculationRecordMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalculationRecordServiceTest {

    @Mock
    private CalculationRecordMapper calculationRecordMapper;

    @Mock
    private AchievementService achievementService;

    @Mock
    private ReferralService referralService;

    @InjectMocks
    private CalculationRecordService calculationRecordService;

    @Test
    void getUserRecordShouldRejectAccessToOtherUsersRecord() {
        when(calculationRecordMapper.selectByIdAndUserId(10L, 7L)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> calculationRecordService.getUserRecord(7L, 10L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void getUserRecordsPagedShouldUseTypedPagingWhenTypeProvided() {
        when(calculationRecordMapper.selectByUserIdAndTypePaged(7L, "tarot", 20, 20))
                .thenReturn(List.of(TbCalculationRecord.builder().id(1L).recordType("tarot").build()));

        List<TbCalculationRecord> records = calculationRecordService.getUserRecordsPaged(7L, "tarot", 2, 20);

        assertEquals(1, records.size());
        verify(calculationRecordMapper).selectByUserIdAndTypePaged(7L, "tarot", 20, 20);
    }

    @Test
    void saveRecordShouldNormalizeMissingTitleAndData() {
        TbCalculationRecord record = TbCalculationRecord.builder()
                .userId(7L)
                .recordType("Tarot")
                .recordTitle("   ")
                .data(null)
                .build();

        calculationRecordService.saveRecord(record);

        assertEquals("tarot", record.getRecordType());
        assertEquals("未命名记录", record.getRecordTitle());
        assertEquals("{}", record.getData());
        verify(calculationRecordMapper).insert(record);
        verify(referralService).recordFirstDivination(7L);
    }
}
