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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
        // Nếu dùng constructor injection, hãy truyền đúng constructor,
        // nếu dùng field injection, không cần.
        ride = new Ride();
        ride.setId(10L);
        ride.setUserId(10L);
        ride.setDistanceKm(12.0);
        ride.setStatus("completed");
        ride.setCompletedAt(LocalDateTime.now().minusDays(1));
    }

    @AfterEach
    void tearDown() {}

    @Test
    void shouldCalculateCorrectRewardPoints() {
        Page<Ride> ridePage = new PageImpl<>(List.of(ride));
        when(rideRepository.findByStatusAndCompletedAtBetween(eq("completed"), any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(ridePage)
                .thenReturn(Page.empty()); // Để kết thúc vòng lặp

        when(rewardTransactionRepository.existsByUserIdAndRideIdAndType(eq(10L), eq(10L), eq("self")))
                .thenReturn(false);
        when(userRepository.findReferrerIdByUserId(10L)).thenReturn(null);

        mileageRewardService.processDailyRewards();

        ArgumentCaptor<RewardTransaction> captor = ArgumentCaptor.forClass(RewardTransaction.class);
        verify(rewardTransactionRepository).save(captor.capture());

        RewardTransaction reward = captor.getValue();
        assertEquals(0, reward.getPoints().compareTo(BigDecimal.valueOf(0.12)));
        assertEquals("self", reward.getType());
    }

    @Test
    void shouldLogErrorWhenSavingRewardFails() {
        Page<Ride> ridePage = new PageImpl<>(List.of(ride));
        when(rideRepository.findByStatusAndCompletedAtBetween(eq("completed"), any(), any(), any()))
                .thenReturn(ridePage)
                .thenReturn(Page.empty());

        when(rewardTransactionRepository.existsByUserIdAndRideIdAndType(any(), any(), any()))
                .thenReturn(false);

        lenient().when(userRepository.findReferrerIdByUserId(anyLong())).thenReturn(null);

        doThrow(new RuntimeException("DB error"))
                .when(rewardTransactionRepository).save(any());

        assertDoesNotThrow(() -> mileageRewardService.processDailyRewards());

        verify(rewardTransactionRepository).save(any());
    }

    @Test
    void shouldSaveBothSelfAndReferralRewards() {
        Page<Ride> ridePage = new PageImpl<>(List.of(ride));
        when(rideRepository.findByStatusAndCompletedAtBetween(eq("completed"), any(), any(), any()))
                .thenReturn(ridePage)
                .thenReturn(Page.empty());

        when(rewardTransactionRepository.existsByUserIdAndRideIdAndType(eq(10L), eq(10L), eq("self")))
                .thenReturn(false);
        when(rewardTransactionRepository.existsByUserIdAndRideIdAndType(eq(2002L), eq(10L), eq("referral")))
                .thenReturn(false);
        when(userRepository.findReferrerIdByUserId(10L)).thenReturn(2002L);

        mileageRewardService.processDailyRewards();

        verify(rewardTransactionRepository, times(2)).save(any());
    }

    @Test
    void testProcessDailyRewards_withEmptyRides() {
        when(rideRepository.findByStatusAndCompletedAtBetween(eq("completed"), any(), any(), any()))
                .thenReturn(Page.empty());

        mileageRewardService.processDailyRewards();

        verify(rewardTransactionRepository, never()).save(any());
    }

    @Test
    void processDailyRewards() {
        // Should not throw exception even when there is no data
        when(rideRepository.findByStatusAndCompletedAtBetween(eq("completed"), any(), any(), any()))
                .thenReturn(Page.empty());

        assertDoesNotThrow(() -> mileageRewardService.processDailyRewards());
    }
}
