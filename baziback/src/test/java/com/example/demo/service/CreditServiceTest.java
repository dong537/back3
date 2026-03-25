package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.mapper.CreditMapper;
import com.example.demo.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreditServiceTest {

    @Mock
    private CreditMapper creditMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private SseEmitterService sseEmitterService;

    @InjectMocks
    private CreditService creditService;

    @Test
    void getCurrentPointsShouldInitializeCreditRowFromUserTable() {
        Long userId = 9L;

        when(creditMapper.getBalanceByUserId(userId)).thenReturn(null, 66);
        when(userMapper.findById(userId)).thenReturn(User.builder().id(userId).currentPoints(66).build());

        Integer balance = creditService.getCurrentPoints(userId);

        assertEquals(66, balance);
        verify(creditMapper).initUserCredit(userId, 66);
    }

    @Test
    void deductPointsShouldStopWhenBalanceIsNotEnough() {
        Long userId = 9L;

        when(creditMapper.getBalanceByUserIdForUpdate(userId)).thenReturn(15);

        boolean success = creditService.deductPoints(userId, 20, "AI解读");

        assertFalse(success);
        verify(creditMapper, never()).deductPointsIfEnough(eq(userId), anyInt());
        verify(userMapper, never()).updatePoints(eq(userId), anyInt());
    }
}
