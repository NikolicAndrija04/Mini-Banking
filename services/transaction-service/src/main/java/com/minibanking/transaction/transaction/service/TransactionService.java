package com.minibanking.transaction.transaction.service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

import com.minibanking.transaction.client.account.AccountClient;
import com.minibanking.transaction.client.account.AccountTransferRequest;
import com.minibanking.transaction.client.account.AccountTransferResponse;
import com.minibanking.transaction.common.error.ConflictException;
import com.minibanking.transaction.common.error.ResourceNotFoundException;
import com.minibanking.transaction.transaction.api.CreateTransferRequest;
import com.minibanking.transaction.transaction.api.TransactionResponse;
import com.minibanking.transaction.transaction.api.UpdateTransactionRequest;
import com.minibanking.transaction.transaction.domain.BankTransaction;
import com.minibanking.transaction.transaction.domain.TransactionStatus;
import com.minibanking.transaction.transaction.repository.BankTransactionRepository;
import com.minibanking.transaction.messaging.TransferCompletedDomainEvent;

@Service
public class TransactionService {

    private final BankTransactionRepository transactionRepository;
    private final AccountClient accountClient;
    private final ApplicationEventPublisher eventPublisher;

    public TransactionService(
            BankTransactionRepository transactionRepository,
            AccountClient accountClient,
            ApplicationEventPublisher eventPublisher
    ) {
        this.transactionRepository = transactionRepository;
        this.accountClient = accountClient;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TransactionResponse createAndExecute(CreateTransferRequest request) {
        BankTransaction existing = transactionRepository.findByIdempotencyKey(request.idempotencyKey()).orElse(null);
        if (existing != null) {
            if (!existing.matches(request.sourceAccountId(), request.destinationAccountId(), request.amount())) {
                throw new ConflictException("Idempotency key is already used for a different transfer");
            }
            return toResponse(existing, true);
        }
        if (request.sourceAccountId().equals(request.destinationAccountId())) {
            throw new ConflictException("Source and destination accounts must be different");
        }

        BankTransaction transaction = transactionRepository.saveAndFlush(new BankTransaction(
                request.idempotencyKey(),
                request.sourceAccountId(),
                request.destinationAccountId(),
                request.amount(),
                normalizeDescription(request.description())
        ));
        execute(transaction);
        return toResponse(transaction, false);
    }

    @Transactional(readOnly = true)
    public TransactionResponse findById(UUID id) {
        return toResponse(requireTransaction(id), false);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> findAll(UUID accountId, TransactionStatus status) {
        return transactionRepository.findAll().stream()
                .filter(transaction -> accountId == null
                        || transaction.getSourceAccountId().equals(accountId)
                        || transaction.getDestinationAccountId().equals(accountId))
                .filter(transaction -> status == null || transaction.getStatus() == status)
                .sorted(Comparator.comparing(BankTransaction::getCreatedAt).reversed())
                .map(transaction -> toResponse(transaction, false))
                .toList();
    }

    @Transactional
    public TransactionResponse update(UUID id, UpdateTransactionRequest request) {
        BankTransaction transaction = requireTransaction(id);
        transaction.updateDescription(normalizeDescription(request.description()));
        return toResponse(transaction, false);
    }

    @Transactional
    public TransactionResponse retry(UUID id) {
        BankTransaction transaction = requireTransaction(id);
        if (transaction.getStatus() != TransactionStatus.FAILED) {
            throw new ConflictException("Only a failed transaction can be retried");
        }
        transaction.prepareRetry();
        execute(transaction);
        return toResponse(transaction, false);
    }

    @Transactional
    public void delete(UUID id) {
        BankTransaction transaction = requireTransaction(id);
        if (transaction.getStatus() != TransactionStatus.FAILED) {
            throw new ConflictException("Completed and pending transaction audit records cannot be deleted");
        }
        transactionRepository.delete(transaction);
    }

    private void execute(BankTransaction transaction) {
        try {
            AccountTransferResponse response = accountClient.transfer(new AccountTransferRequest(
                    transaction.getId(),
                    transaction.getSourceAccountId(),
                    transaction.getDestinationAccountId(),
                    transaction.getAmount()
            ));
            transaction.complete(
                    response.sourceCustomerId(),
                    response.destinationCustomerId(),
                    response.currencyCode(),
                    response.sourceBalanceAfter(),
                    response.destinationBalanceAfter(),
                    response.completedAt()
            );
            eventPublisher.publishEvent(new TransferCompletedDomainEvent(
                    transaction.getId(),
                    transaction.getSourceCustomerId(),
                    transaction.getDestinationCustomerId(),
                    transaction.getSourceAccountId(),
                    transaction.getDestinationAccountId(),
                    transaction.getAmount(),
                    transaction.getCurrencyCode(),
                    transaction.getCompletedAt()
            ));
        } catch (RuntimeException exception) {
            transaction.fail(failureMessage(exception));
        }
    }

    private BankTransaction requireTransaction(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction " + id + " was not found"));
    }

    private String normalizeDescription(String description) {
        return description == null ? null : description.trim();
    }

    private String failureMessage(RuntimeException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return "Account service rejected or could not process the transfer";
        }
        return message.length() <= 500 ? message : message.substring(0, 500);
    }

    private TransactionResponse toResponse(BankTransaction transaction, boolean replay) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getIdempotencyKey(),
                transaction.getSourceAccountId(),
                transaction.getDestinationAccountId(),
                transaction.getAmount(),
                transaction.getCurrencyCode(),
                transaction.getDescription(),
                transaction.getStatus(),
                transaction.getFailureReason(),
                transaction.getSourceBalanceAfter(),
                transaction.getDestinationBalanceAfter(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt(),
                transaction.getCompletedAt(),
                replay
        );
    }
}
