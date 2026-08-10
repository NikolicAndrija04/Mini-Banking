package com.minibanking.account.account.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minibanking.account.account.api.AccountResponse;
import com.minibanking.account.account.api.CreateAccountRequest;
import com.minibanking.account.account.api.InternalTransferRequest;
import com.minibanking.account.account.api.InternalTransferResponse;
import com.minibanking.account.account.api.MoneyRequest;
import com.minibanking.account.account.api.UpdateAccountRequest;
import com.minibanking.account.account.domain.Account;
import com.minibanking.account.account.domain.AccountStatus;
import com.minibanking.account.account.domain.AccountTransferRecord;
import com.minibanking.account.account.repository.AccountRepository;
import com.minibanking.account.account.repository.AccountTransferRecordRepository;
import com.minibanking.account.common.error.ConflictException;
import com.minibanking.account.common.error.ResourceNotFoundException;
import com.minibanking.account.config.AccountProperties;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountTransferRecordRepository transferRecordRepository;
    private final AccountProperties accountProperties;

    public AccountService(
            AccountRepository accountRepository,
            AccountTransferRecordRepository transferRecordRepository,
            AccountProperties accountProperties
    ) {
        this.accountRepository = accountRepository;
        this.transferRecordRepository = transferRecordRepository;
        this.accountProperties = accountProperties;
    }

    @Transactional
    public AccountResponse create(CreateAccountRequest request) {
        String accountNumber = normalizeAccountNumber(request.accountNumber());
        if (accountRepository.existsByAccountNumber(accountNumber)) {
            throw new ConflictException("Account number already exists");
        }
        Account account = new Account(
                request.customerId(),
                accountNumber,
                request.type(),
                request.currencyCode().toUpperCase(),
                request.initialBalance(),
                AccountStatus.ACTIVE
        );
        return toResponse(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public AccountResponse findById(UUID id) {
        return toResponse(requireAccount(id));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> findAll(UUID customerId) {
        List<Account> accounts = customerId == null
                ? accountRepository.findAll()
                : accountRepository.findAllByCustomerIdOrderByCreatedAtDesc(customerId);
        return accounts.stream().map(this::toResponse).toList();
    }

    @Transactional
    public AccountResponse update(UUID id, UpdateAccountRequest request) {
        Account account = requireAccount(id);
        String accountNumber = normalizeAccountNumber(request.accountNumber());
        if (accountRepository.existsByAccountNumberAndIdNot(accountNumber, id)) {
            throw new ConflictException("Account number already exists");
        }
        if (request.status() == AccountStatus.CLOSED && account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new ConflictException("Only an account with zero balance can be closed");
        }
        account.update(request.customerId(), accountNumber, request.type(), request.status());
        return toResponse(account);
    }

    @Transactional
    public AccountResponse deposit(UUID id, MoneyRequest request) {
        Account account = lockAccount(id);
        requireWithinTransferLimit(request.amount());
        account.credit(request.amount());
        return toResponse(account);
    }

    @Transactional
    public AccountResponse withdraw(UUID id, MoneyRequest request) {
        Account account = lockAccount(id);
        requireWithinTransferLimit(request.amount());
        account.debit(request.amount());
        return toResponse(account);
    }

    @Transactional
    public void delete(UUID id) {
        Account account = requireAccount(id);
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new ConflictException("Only an account with zero balance can be deleted");
        }
        accountRepository.delete(account);
    }

    @Transactional
    public InternalTransferResponse transfer(InternalTransferRequest request) {
        AccountTransferRecord existing = transferRecordRepository.findById(request.transferId()).orElse(null);
        if (existing != null) {
            if (!existing.matches(request.sourceAccountId(), request.destinationAccountId(), request.amount())) {
                throw new ConflictException("Transfer id is already used for a different request");
            }
            return toTransferResponse(existing, true);
        }

        if (request.sourceAccountId().equals(request.destinationAccountId())) {
            throw new ConflictException("Source and destination accounts must be different");
        }
        requireWithinTransferLimit(request.amount());

        List<UUID> lockOrder = List.of(request.sourceAccountId(), request.destinationAccountId()).stream()
                .sorted(Comparator.comparing(UUID::toString))
                .toList();
        Account first = lockAccount(lockOrder.get(0));
        Account second = lockAccount(lockOrder.get(1));
        Account source = first.getId().equals(request.sourceAccountId()) ? first : second;
        Account destination = first.getId().equals(request.destinationAccountId()) ? first : second;

        source.requireActive();
        destination.requireActive();
        if (!source.getCurrencyCode().equals(destination.getCurrencyCode())) {
            throw new ConflictException("Accounts must use the same currency");
        }

        source.debit(request.amount());
        destination.credit(request.amount());
        AccountTransferRecord completed = transferRecordRepository.save(new AccountTransferRecord(
                request.transferId(),
                source.getId(),
                destination.getId(),
                request.amount(),
                source.getCurrencyCode(),
                source.getBalance(),
                destination.getBalance()
        ));
        return toTransferResponse(completed, false);
    }

    @Transactional(readOnly = true)
    public InternalTransferResponse findTransfer(UUID transferId) {
        AccountTransferRecord record = transferRecordRepository.findById(transferId)
                .orElseThrow(() -> new ResourceNotFoundException("Transfer record " + transferId + " was not found"));
        return toTransferResponse(record, false);
    }

    private Account requireAccount(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account " + id + " was not found"));
    }

    private Account lockAccount(UUID id) {
        return accountRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account " + id + " was not found"));
    }

    private void requireWithinTransferLimit(BigDecimal amount) {
        if (amount.compareTo(accountProperties.maxTransferAmount()) > 0) {
            throw new ConflictException("Amount exceeds the configured maximum of " + accountProperties.maxTransferAmount());
        }
    }

    private String normalizeAccountNumber(String value) {
        return value.replace(" ", "").toUpperCase();
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getCustomerId(),
                account.getAccountNumber(),
                account.getType(),
                account.getCurrencyCode(),
                account.getBalance(),
                account.getStatus(),
                account.getCreatedAt(),
                account.getUpdatedAt()
        );
    }

    private InternalTransferResponse toTransferResponse(AccountTransferRecord record, boolean replay) {
        return new InternalTransferResponse(
                record.getTransferId(),
                record.getSourceAccountId(),
                record.getDestinationAccountId(),
                record.getAmount(),
                record.getCurrencyCode(),
                record.getSourceBalanceAfter(),
                record.getDestinationBalanceAfter(),
                record.getCompletedAt(),
                replay
        );
    }
}
