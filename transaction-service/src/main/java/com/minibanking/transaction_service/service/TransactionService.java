package com.minibanking.transaction_service.service;

import com.minibanking.transaction_service.client.AccountClient;
import com.minibanking.transaction_service.dto.AccountResponse;
import com.minibanking.transaction_service.dto.BalanceRequest;
import com.minibanking.transaction_service.dto.TransactionResponse;
import com.minibanking.transaction_service.dto.TransferRequest;
import com.minibanking.transaction_service.entity.Transaction;
import com.minibanking.transaction_service.entity.TransactionStatus;
import com.minibanking.transaction_service.entity.TransactionType;
import com.minibanking.transaction_service.exception.AccountServiceUnavailableException;
import com.minibanking.transaction_service.exception.InvalidTransferException;
import com.minibanking.transaction_service.repository.TransactionRepository;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.minibanking.transaction_service.dto.TransactionDescriptionRequest;
import com.minibanking.transaction_service.exception.TransactionNotFoundException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountClient accountClient;
    private static final Logger log =
            LoggerFactory.getLogger(TransactionService.class);
    public TransactionService(
            TransactionRepository transactionRepository,
            AccountClient accountClient
    ) {
        this.transactionRepository = transactionRepository;
        this.accountClient = accountClient;
    }

    @Retry(name = "accountService")
    public TransactionResponse transfer(TransferRequest request) {
        log.info("Executing transfer operation...");
        if (request.getSourceAccountId()
                .equals(request.getDestinationAccountId())) {
            throw new InvalidTransferException(
                    "Source and destination account cannot be the same"
            );
        }

        AccountResponse sourceAccount =
                accountClient.getAccountById(request.getSourceAccountId());

        AccountResponse destinationAccount =
                accountClient.getAccountById(request.getDestinationAccountId());

        if (!sourceAccount.getCurrency()
                .equals(destinationAccount.getCurrency())) {
            throw new InvalidTransferException(
                    "Accounts must use the same currency"
            );
        }

        Transaction transaction = new Transaction(
                request.getSourceAccountId(),
                request.getDestinationAccountId(),
                request.getAmount(),
                TransactionType.TRANSFER,
                TransactionStatus.PENDING,
                request.getDescription(),
                LocalDateTime.now()
        );

        transaction = transactionRepository.save(transaction);

        accountClient.withdraw(
                request.getSourceAccountId(),
                new BalanceRequest(request.getAmount())
        );

        accountClient.deposit(
                request.getDestinationAccountId(),
                new BalanceRequest(request.getAmount())
        );

        transaction.setStatus(TransactionStatus.COMPLETED);

        Transaction savedTransaction =
                transactionRepository.save(transaction);

        return mapToResponse(savedTransaction);
    }

    public List<TransactionResponse> getAll() {
        return transactionRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<TransactionResponse> getByAccountId(Long accountId) {
        return transactionRepository
                .findBySourceAccountIdOrDestinationAccountId(
                        accountId,
                        accountId
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }



    private TransactionResponse mapToResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getSourceAccountId(),
                transaction.getDestinationAccountId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }
    public TransactionResponse getById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        return mapToResponse(transaction);
    }

    public TransactionResponse updateDescription(
            Long id,
            TransactionDescriptionRequest request
    ) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        transaction.setDescription(request.getDescription());

        Transaction updatedTransaction =
                transactionRepository.save(transaction);

        return mapToResponse(updatedTransaction);
    }

    public void delete(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));

        transactionRepository.delete(transaction);
    }
}