package com.minibanking.account.account.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minibanking.account.account.domain.AccountTransferRecord;

public interface AccountTransferRecordRepository extends JpaRepository<AccountTransferRecord, UUID> {
}
