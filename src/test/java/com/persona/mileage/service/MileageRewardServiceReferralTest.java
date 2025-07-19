package com.persona.mileage.service;

import com.persona.mileage.entity.RewardTransaction;
import com.persona.mileage.entity.Ride;
import com.persona.mileage.repository.RewardTransactionRepository;
import com.persona.mileage.repository.RideRepository;
import com.persona.mileage.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MileageRewardServiceReferralTest {

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
    void setUp() {
        ride = new Ride();
        ride.setId(1L);
        ride.setUserId(101L);
        ride.setDistanceKm(20.0);
        ride.setStatus("COMPLETED");
        ride.setCompletedAt(LocalDateTime.now().minusDays(1));
    }

    @Test
    void testReferralRewardCreatedIfReferrerExists() {
        when(rideRepository.findByStatusAndCompletedAtBetween(eq("COMPLETED"), any(), any()))
                .thenReturn(List.of(ride));

        when(rewardTransactionRepository.existsByUserIdAndRideIdAndType(eq(101L), eq(1L), eq("SELF")))
                .thenReturn(false);
        when(rewardTransactionRepository.existsByUserIdAndRideIdAndType(eq(999L), eq(1L), eq("REFERRAL")))
                .thenReturn(false);
        when(userRepository.findReferrerIdByUserId(101L)).thenReturn(999L);

        mileageRewardService.processDailyRewards();

        ArgumentCaptor<RewardTransaction> captor = ArgumentCaptor.forClass(RewardTransaction.class);
        verify(rewardTransactionRepository, times(2)).save(captor.capture());

        List<RewardTransaction> rewards = captor.getAllValues();

        RewardTransaction selfReward = rewards.stream().filter(r -> "SELF".equals(r.getType())).findFirst().orElse(null);
        RewardTransaction referralReward = rewards.stream().filter(r -> "REFERRAL".equals(r.getType())).findFirst().orElse(null);

        assertNotNull(selfReward);
        assertEquals(BigDecimal.valueOf(0.2), selfReward.getPoints());
        assertEquals(101L, selfReward.getUserId());

        assertNotNull(referralReward);
        assertEquals(BigDecimal.valueOf(0.02), referralReward.getPoints());
        assertEquals(999L, referralReward.getUserId());
    }

    @Test
    void testNoReferralRewardIfNoReferrer() {
        when(rideRepository.findByStatusAndCompletedAtBetween(eq("COMPLETED"), any(), any()))
                .thenReturn(List.of(ride));
        when(rewardTransactionRepository.existsByUserIdAndRideIdAndType(eq(101L), eq(1L), eq("SELF"))).thenReturn(false);
        when(userRepository.findReferrerIdByUserId(101L)).thenReturn(null);

        mileageRewardService.processDailyRewards();

        ArgumentCaptor<RewardTransaction> captor = ArgumentCaptor.forClass(RewardTransaction.class);
        verify(rewardTransactionRepository, times(1)).save(captor.capture());

        RewardTransaction reward = captor.getValue();
        assertEquals("SELF", reward.getType());
        assertEquals(BigDecimal.valueOf(0.2), reward.getPoints());
        assertEquals(101L, reward.getUserId());
    }
}