package com.minibanking.transaction.transaction.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import com.minibanking.transaction.client.account.AccountClient;
import com.minibanking.transaction.client.account.AccountTransferRequest;
import com.minibanking.transaction.client.account.AccountTransferResponse;
import com.minibanking.transaction.common.error.ResourceNotFoundException;
import com.minibanking.transaction.transaction.api.CreateTransferRequest;
import com.minibanking.transaction.transaction.api.TransactionResponse;
import com.minibanking.transaction.transaction.api.UpdateTransactionRequest;
import com.minibanking.transaction.transaction.domain.TransactionStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Transactional
@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false"
})
class TransactionServiceIntegrationTests {

    @Autowired
    private TransactionService transactionService;

    @MockitoBean
    private AccountClient accountClient;

    @Test
    void executesTransferThroughFeignClientOnlyOnceForRepeatedRequest() {
        when(accountClient.transfer(any())).thenAnswer(invocation -> {
            AccountTransferRequest request = invocation.getArgument(0);
            return new AccountTransferResponse(
                    request.transferId(),
                    request.sourceAccountId(),
                    request.destinationAccountId(),
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    request.amount(),
                    "RSD",
                    new BigDecimal("750.00"),
                    new BigDecimal("350.00"),
                    Instant.now(),
                    false
            );
        });
        CreateTransferRequest request = request();

        TransactionResponse created = transactionService.createAndExecute(request);
        TransactionResponse replay = transactionService.createAndExecute(request);

        assertThat(created.status()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(created.currencyCode()).isEqualTo("RSD");
        assertThat(replay.id()).isEqualTo(created.id());
        assertThat(replay.idempotentReplay()).isTrue();
        verify(accountClient, times(1)).transfer(any());
    }

    @Test
    void supportsCrudForFailedTransactionRecord() {
        when(accountClient.transfer(any())).thenThrow(new IllegalStateException("account-service unavailable"));
        TransactionResponse failed = transactionService.createAndExecute(request());

        assertThat(failed.status()).isEqualTo(TransactionStatus.FAILED);
        assertThat(failed.failureReason()).contains("unavailable");
        TransactionResponse updated = transactionService.update(
                failed.id(),
                new UpdateTransactionRequest("Retry after maintenance")
        );
        assertThat(updated.description()).isEqualTo("Retry after maintenance");
        assertThat(transactionService.findAll(null, TransactionStatus.FAILED))
                .extracting(TransactionResponse::id)
                .contains(failed.id());

        transactionService.delete(failed.id());
        assertThatThrownBy(() -> transactionService.findById(failed.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private CreateTransferRequest request() {
        return new CreateTransferRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("250.00"),
                "Faculty project test transfer"
        );
    }
}
