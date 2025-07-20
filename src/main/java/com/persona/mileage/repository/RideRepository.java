package com.persona.mileage.repository;

import com.persona.mileage.entity.Ride;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {

    // Lấy tất cả các chuyến đi hoàn thành vào ngày hôm qua
    @Query("SELECT r FROM Ride r WHERE r.completedAt BETWEEN :start AND :end")
    List<Ride> findRidesCompletedBetween(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);


    Page<Ride> findByStatusAndCompletedAtBetween(String status, LocalDateTime start, LocalDateTime end, Pageable pageable);
    Page<Ride> findByStatusAndCompletedAtBetweenAndIsRewardFalse(
            String status, LocalDateTime start, LocalDateTime end, Pageable pageable
    );
    // Optional: nếu bạn dùng phương thức tiện lợi
    default List<Ride> findRidesForYesterday() {
        LocalDateTime startOfYesterday = LocalDateTime.now().minusDays(1).toLocalDate().atStartOfDay();
        LocalDateTime endOfYesterday = startOfYesterday.plusDays(1).minusSeconds(1);
        return findRidesCompletedBetween(startOfYesterday, endOfYesterday);
    }
}
