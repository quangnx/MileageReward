package com.persona.mileage.repository;

import com.persona.mileage.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u.referrerId FROM Users u WHERE u.id = :userId")
    Long findReferrerIdByUserId(Long userId);
}