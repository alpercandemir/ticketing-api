package com.example.ticketing.repository;

import com.example.ticketing.domain.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {
    IdempotencyKey findByIdempotencyKey(String key);
}
