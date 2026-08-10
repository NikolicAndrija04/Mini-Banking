package com.minibanking.customer.customer.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.minibanking.customer.common.error.ConflictException;
import com.minibanking.customer.common.error.ResourceNotFoundException;
import com.minibanking.customer.customer.api.CustomerRequest;
import com.minibanking.customer.customer.api.CustomerResponse;
import com.minibanking.customer.customer.domain.CustomerStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest(properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false"
})
class CustomerServiceIntegrationTests {

    @Autowired
    private CustomerService customerService;

    @Test
    void completesCustomerCrudLifecycle() {
        CustomerResponse created = customerService.create(request("andrija@example.com", "Novi Sad"));

        assertThat(created.id()).isNotNull();
        assertThat(created.status()).isEqualTo(CustomerStatus.ACTIVE);
        assertThat(customerService.findById(created.id()).email()).isEqualTo("andrija@example.com");

        CustomerResponse updated = customerService.update(
                created.id(),
                request("andrija@example.com", "Beograd")
        );
        assertThat(updated.address()).isEqualTo("Beograd");
        assertThat(customerService.findAll()).extracting(CustomerResponse::id).contains(created.id());

        customerService.delete(created.id());
        assertThatThrownBy(() -> customerService.findById(created.id()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void rejectsDuplicateEmail() {
        customerService.create(request("duplicate@example.com", "Novi Sad"));

        assertThatThrownBy(() -> customerService.create(request("DUPLICATE@example.com", "Beograd")))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already exists");
    }

    private CustomerRequest request(String email, String address) {
        return new CustomerRequest(
                "Andrija",
                "Nikolic",
                email,
                "+381 64 123 4567",
                address,
                CustomerStatus.ACTIVE
        );
    }
}
