package com.persona.mileage.service;

import com.persona.mileage.entity.Ride;
import com.persona.mileage.repository.RewardTransactionRepository;
import com.persona.mileage.repository.RideRepository;
import com.persona.mileage.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
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
    }

    @Test
    void testReferralRewardCreatedIfReferrerExists() {
        when(rideRepository.findByStatusAndCompletedAtBetween(eq("COMPLETED"), any(), any()))
                .thenReturn(List.of(ride));
        when(rewardTransactionRepository.existsByUserIdAndRideIdAndType(any(), any(), any())).thenReturn(false);
        when(userRepository.findReferrerIdByUserId(101L)).thenReturn(999L);

        mileageRewardService.processDailyRewards();

        verify(rewardTransactionRepository, times(2)).save(any()); // One for SELF, one for REFERRAL
    }

    @Test
    void testNoReferralRewardIfNoReferrer() {
        when(rideRepository.findByStatusAndCompletedAtBetween(eq("COMPLETED"), any(), any()))
                .thenReturn(List.of(ride));
        when(rewardTransactionRepository.existsByUserIdAndRideIdAndType(any(), any(), any())).thenReturn(false);
        when(userRepository.findReferrerIdByUserId(101L)).thenReturn(null);

        mileageRewardService.processDailyRewards();

        verify(rewardTransactionRepository, times(1)).save(any()); // Only SELF reward
    }
}