package com.minibanking.account_service.service;
import com.minibanking.account_service.dto.AccountDetailsResponse;
import com.minibanking.account_service.client.CustomerClient;
import com.minibanking.account_service.dto.AccountRequest;
import com.minibanking.account_service.dto.AccountResponse;
import com.minibanking.account_service.dto.CustomerResponse;
import com.minibanking.account_service.entity.Account;
import com.minibanking.account_service.entity.AccountStatus;
import com.minibanking.account_service.exception.AccountNotActiveException;
import com.minibanking.account_service.exception.AccountNotFoundException;
import com.minibanking.account_service.exception.CustomerNotFoundException;
import com.minibanking.account_service.exception.InsufficientFundsException;
import com.minibanking.account_service.repository.AccountRepository;
import feign.FeignException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerClient customerClient;


    public AccountService(
            AccountRepository accountRepository,
            CustomerClient customerClient
    ) {
        this.accountRepository = accountRepository;
        this.customerClient = customerClient;
    }

    public AccountResponse deposit(Long id, BigDecimal amount) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(id);
        }

        account.setBalance(
                account.getBalance().add(amount)
        );

        return mapToResponse(
                accountRepository.save(account)
        );
    }

    public AccountResponse updateStatus(Long id, AccountStatus status) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        account.setStatus(status);

        return mapToResponse(
                accountRepository.save(account)
        );
    }

    public AccountResponse withdraw(Long id, BigDecimal amount) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(id);
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException(id);
        }

        account.setBalance(
                account.getBalance().subtract(amount)
        );

        return mapToResponse(
                accountRepository.save(account)
        );
    }

    public AccountResponse create(AccountRequest request) {

        try {
            CustomerResponse customer =
                    customerClient.getCustomerById(request.getCustomerId());

            if (customer == null) {
                throw new CustomerNotFoundException(request.getCustomerId());
            }

        } catch (FeignException.NotFound exception) {
            throw new CustomerNotFoundException(request.getCustomerId());
        }

        Account account = new Account(
                generateAccountNumber(),
                request.getCustomerId(),
                BigDecimal.ZERO,
                request.getCurrency(),
                AccountStatus.ACTIVE,
                LocalDateTime.now()
        );

        Account savedAccount = accountRepository.save(account);

        return mapToResponse(savedAccount);
    }

    public List<AccountResponse> getAll() {
        return accountRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AccountResponse getById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        return mapToResponse(account);
    }

    public List<AccountResponse> getByCustomerId(Long customerId) {
        return accountRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public void delete(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        accountRepository.delete(account);
    }

    private AccountResponse mapToResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getCustomerId(),
                account.getBalance(),
                account.getCurrency(),
                account.getStatus(),
                account.getCreatedAt()
        );
    }

    public AccountDetailsResponse getDetails(Long id) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));

        CustomerResponse customer;

        try {
            customer = customerClient.getCustomerById(
                    account.getCustomerId()
            );
        } catch (FeignException.NotFound exception) {
            throw new CustomerNotFoundException(
                    account.getCustomerId()
            );
        }

        return new AccountDetailsResponse(
                mapToResponse(account),
                customer
        );
    }

    private String generateAccountNumber() {
        return "RS" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 18)
                .toUpperCase();
    }
}
