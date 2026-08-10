package com.minibanking.account.account.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.minibanking.account.account.domain.Account;

import jakarta.persistence.LockModeType;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    boolean existsByAccountNumber(String accountNumber);

    boolean existsByAccountNumberAndIdNot(String accountNumber, UUID id);

    List<Account> findAllByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select account from Account account where account.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") UUID id);
}
