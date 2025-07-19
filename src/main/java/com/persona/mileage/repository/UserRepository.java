package com.persona.mileage.repository;

public interface UserRepository {
    Long findReferrerIdByUserId(Long userId);
}