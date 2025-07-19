package com.persona.mileage.service;

import com.persona.mileage.entity.RewardTransaction;
import com.persona.mileage.entity.Ride;
import com.persona.mileage.repository.RewardTransactionRepository;
import com.persona.mileage.repository.RideRepository;
import com.persona.mileage.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class MileageRewardServiceTest {

    @InjectMocks
    private MileageRewardService mileageRewardService;

    @Mock
    private RideRepository rideRepository;

    @Mock
    private RewardTransactionRepository rewardTransactionRepository;
    @Mock
    private UserRepository userRepository;
    private Ride ride;
    @BeforeEach
    void setup() {
        ride = new Ride();
        ride.setId(10L);
        ride.setUserId(1001L);
        ride.setDistanceKm(50.0);
        ride.setStatus("COMPLETED");
        ride.setCompletedAt(LocalDateTime.now().minusDays(1));
    }

    @AfterEach
    void tearDown() {
    }

    // Kiểm tra chính xác 1% quãng đường = điểm thưởng
    @Test
    void shouldCalculateCorrectRewardPoints() {
        when(rideRepository.findByStatusAndCompletedAtBetween(eq("COMPLETED"), any(), any()))
                .thenReturn(List.of(ride));
        when(rewardTransactionRepository.existsByUserIdAndRideIdAndType(any(), any(), any()))
                .thenReturn(false);
        when(userRepository.findReferrerIdByUserId(1001L)).thenReturn(null);

        mileageRewardService.processDailyRewards();

        ArgumentCaptor<RewardTransaction> captor = ArgumentCaptor.forClass(RewardTransaction.class);
        verify(rewardTransactionRepository).save(captor.capture());

        RewardTransaction reward = captor.getValue();
        assertEquals(BigDecimal.valueOf(0.5), reward.getPoints()); // 1% of 50km
        assertEquals("SELF", reward.getType());
    }


    // Mô phỏng lỗi DB và kiểm tra không ném exception
    @Test
    void shouldLogErrorWhenSavingRewardFails() {
        when(rideRepository.findByStatusAndCompletedAtBetween(eq("COMPLETED"), any(), any()))
                .thenReturn(List.of(ride));
        when(rewardTransactionRepository.existsByUserIdAndRideIdAndType(any(), any(), any()))
                .thenReturn(false);
        when(userRepository.findReferrerIdByUserId(1001L)).thenReturn(null);
        doThrow(new RuntimeException("DB error"))
                .when(rewardTransactionRepository).save(any());

        assertDoesNotThrow(() -> mileageRewardService.processDailyRewards());
        // Kiểm tra đã gọi rewardTransactionRepository.save()
        verify(rewardTransactionRepository).save(any());
    }

    // Thêm thưởng referral khi có referrer_id
    @Test
    void shouldSaveBothSelfAndReferralRewards() {
        when(rideRepository.findByStatusAndCompletedAtBetween(eq("COMPLETED"), any(), any()))
                .thenReturn(List.of(ride));
        when(rewardTransactionRepository.existsByUserIdAndRideIdAndType(any(), any(), any()))
                .thenReturn(false);
        when(userRepository.findReferrerIdByUserId(1001L)).thenReturn(2002L);

        mileageRewardService.processDailyRewards();

        verify(rewardTransactionRepository, times(2)).save(any()); // SELF + REFERRAL
    }

    @Test
    void testProcessDailyRewards_withEmptyRides() {
        when(rideRepository.findByStatusAndCompletedAtBetween(
                eq("COMPLETED"), any(), any())).thenReturn(Collections.emptyList());

        mileageRewardService.processDailyRewards();

        verify(rewardTransactionRepository, never()).save(any());
    }

    @Test
    void processDailyRewards() {
    }
}