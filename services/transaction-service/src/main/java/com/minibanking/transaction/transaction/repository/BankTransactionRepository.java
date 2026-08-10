package com.minibanking.transaction.transaction.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minibanking.transaction.transaction.domain.BankTransaction;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, UUID> {

    Optional<BankTransaction> findByIdempotencyKey(UUID idempotencyKey);
}
