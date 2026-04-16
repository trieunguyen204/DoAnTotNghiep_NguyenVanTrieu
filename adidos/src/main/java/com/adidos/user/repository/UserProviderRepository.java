package com.adidos.user.repository;

import com.adidos.user.entity.UserProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserProviderRepository extends JpaRepository<UserProvider, Long> {
    @Query("""
                SELECT up FROM UserProvider up
                JOIN FETCH up.user
                WHERE up.provider = :provider AND up.providerId = :providerId
            """)
    Optional<UserProvider> findByProviderAndProviderId(
            @Param("provider") String provider,
            @Param("providerId") String providerId
    );
}