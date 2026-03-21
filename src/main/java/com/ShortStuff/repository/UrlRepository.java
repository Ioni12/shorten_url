package com.ShortStuff.repository;

import com.ShortStuff.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {
    Optional<Url> findByCode(String code);

    Optional<Url> findByLongUrlAndExpiresAtAfter(String longUrl, LocalDateTime now);

    @Modifying
    @Query("DELETE FROM Url u WHERE u.expiresAt < :now")
    void deleteAllExpired(@Param("now") LocalDateTime now);
}
