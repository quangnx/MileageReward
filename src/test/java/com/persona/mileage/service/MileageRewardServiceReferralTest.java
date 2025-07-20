package com.persona.mileage.service;

import com.persona.mileage.entity.RewardTransaction;
import com.persona.mileage.entity.Ride;
import com.persona.mileage.entity.User;
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

import static org.junit.jupiter.api.Assertions.*;
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
        // MockitoAnnotations.openMocks(this);
        mileageRewardService = new MileageRewardService(
                rideRepository,
                rewardTransactionRepository,
                userRepository // truyền đúng mock vào đây
        );

        ride = new Ride();
        ride.setId(1L);
        ride.setUserId(1L);
        ride.setDistanceKm(20.0);
        ride.setStatus("completed");
        ride.setCompletedAt(LocalDateTime.now().minusDays(1));
    }

    @Test
    void testReferralRewardCreatedIfReferrerExists() {
        Ride ride = new Ride();
        ride.setId(1L);
        ride.setUserId(1L);
        ride.setDistanceKm(12.0);

        // Setup User
        User referredUser = new User();
        referredUser.setId(101L);
        referredUser.setReferrerId(202L); // Referrer


        when(rideRepository.findRidesForYesterday()).thenReturn(List.of(ride));
        when(userRepository.findReferrerIdByUserId(ride.getUserId())).thenReturn(1L); // referrer exists
        when(rewardTransactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mileageRewardService.processDailyRewards();

        System.out.println("ride.getUserId(): " + ride.getUserId());

        ArgumentCaptor<RewardTransaction> captor = ArgumentCaptor.forClass(RewardTransaction.class);
        verify(rewardTransactionRepository, times(2)).save(captor.capture());

        List<RewardTransaction> saved = captor.getAllValues();
        assertEquals(10L, saved.get(0).getUserId());
        assertEquals(10L, saved.get(1).getUserId());
    }

    @Test
    void testNoReferralRewardIfNoReferrer() {
        Ride ride = new Ride();
        ride.setId(1L);
        ride.setUserId(1L);
        ride.setDistanceKm(12.0);

        when(rideRepository.findRidesForYesterday()).thenReturn(List.of(ride));
        when(userRepository.findReferrerIdByUserId(ride.getUserId())).thenReturn(null);
        when(rewardTransactionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mileageRewardService.processDailyRewards();

        // only one reward for the rider, none for referral
        verify(rewardTransactionRepository, times(1)).save(any());
    }
}