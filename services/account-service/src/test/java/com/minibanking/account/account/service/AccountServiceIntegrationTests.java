package com.minibanking.account.account.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.minibanking.account.account.api.AccountResponse;
import com.minibanking.account.account.api.CreateAccountRequest;
import com.minibanking.account.account.api.InternalTransferRequest;
import com.minibanking.account.account.api.InternalTransferResponse;
import com.minibanking.account.account.api.MoneyRequest;
import com.minibanking.account.account.api.UpdateAccountRequest;
import com.minibanking.account.account.domain.AccountStatus;
import com.minibanking.account.account.domain.AccountType;
import com.minibanking.account.common.error.ConflictException;
import com.minibanking.account.common.error.ResourceNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false"
})
class AccountServiceIntegrationTests {

    @Autowired
    private AccountService accountService;

    @Test
    void completesZeroBalanceAccountCrudLifecycle() {
        UUID customerId = UUID.randomUUID();
        AccountResponse created = accountService.create(request(customerId, "RS35105008123123123173", "0.00"));

        assertThat(created.id()).isNotNull();
        assertThat(created.status()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(accountService.findAll(customerId)).extracting(AccountResponse::id).contains(created.id());

        AccountResponse updated = accountService.update(
                created.id(),
                new UpdateAccountRequest(customerId, created.accountNumber(), AccountType.SAVINGS, AccountStatus.CLOSED)
        );
        assertThat(updated.type()).isEqualTo(AccountType.SAVINGS);
        assertThat(updated.status()).isEqualTo(AccountStatus.CLOSED);

        accountService.delete(created.id());
        assertThatThrownBy(() -> accountService.findById(created.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void transfersFundsExactlyOnceForRepeatedTransferId() {
        UUID customerId = UUID.randomUUID();
        AccountResponse source = accountService.create(request(customerId, "RS35105008123123123174", "1000.00"));
        AccountResponse destination = accountService.create(request(customerId, "RS35105008123123123175", "100.00"));
        UUID transferId = UUID.randomUUID();
        InternalTransferRequest transfer = new InternalTransferRequest(
                transferId,
                source.id(),
                destination.id(),
                new BigDecimal("250.00")
        );

        InternalTransferResponse firstResult = accountService.transfer(transfer);
        InternalTransferResponse replay = accountService.transfer(transfer);

        assertThat(firstResult.idempotentReplay()).isFalse();
        assertThat(replay.idempotentReplay()).isTrue();
        assertThat(accountService.findById(source.id()).balance()).isEqualByComparingTo("750.00");
        assertThat(accountService.findById(destination.id()).balance()).isEqualByComparingTo("350.00");
    }

    @Test
    void rejectsWithdrawalWhenBalanceIsInsufficient() {
        AccountResponse account = accountService.create(
                request(UUID.randomUUID(), "RS35105008123123123176", "20.00")
        );

        assertThatThrownBy(() -> accountService.withdraw(account.id(), new MoneyRequest(new BigDecimal("20.01"))))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("insufficient funds");
        assertThat(accountService.findById(account.id()).balance()).isEqualByComparingTo("20.00");
    }

    private CreateAccountRequest request(UUID customerId, String accountNumber, String balance) {
        return new CreateAccountRequest(
                customerId,
                accountNumber,
                AccountType.CHECKING,
                "RSD",
                new BigDecimal(balance)
        );
    }
}
