package com.minibanking.customer.customer.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minibanking.customer.common.error.ConflictException;
import com.minibanking.customer.common.error.ResourceNotFoundException;
import com.minibanking.customer.customer.api.CustomerRequest;
import com.minibanking.customer.customer.api.CustomerResponse;
import com.minibanking.customer.customer.domain.Customer;
import com.minibanking.customer.customer.repository.CustomerRepository;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public CustomerResponse create(CustomerRequest request) {
        String email = normalizeEmail(request.email());
        if (customerRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("A customer with email '%s' already exists".formatted(email));
        }

        Customer customer = new Customer(
                request.firstName(),
                request.lastName(),
                email,
                request.phone(),
                request.address(),
                request.status()
        );
        return toResponse(customerRepository.save(customer));
    }

    @Transactional(readOnly = true)
    public CustomerResponse findById(UUID id) {
        return toResponse(requireCustomer(id));
    }

    @Transactional(readOnly = true)
    public CustomerResponse findByEmail(String email) {
        Customer customer = customerRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer with email '%s' was not found".formatted(email)
                ));
        return toResponse(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> findAll() {
        return customerRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    public CustomerResponse update(UUID id, CustomerRequest request) {
        Customer customer = requireCustomer(id);
        String email = normalizeEmail(request.email());
        if (customerRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ConflictException("A customer with email '%s' already exists".formatted(email));
        }

        customer.update(
                request.firstName(),
                request.lastName(),
                email,
                request.phone(),
                request.address(),
                request.status()
        );
        return toResponse(customer);
    }

    public void delete(UUID id) {
        customerRepository.delete(requireCustomer(id));
    }

    private Customer requireCustomer(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer with id '%s' was not found".formatted(id)
                ));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getStatus(),
                customer.getCreatedAt(),
                customer.getUpdatedAt(),
                customer.getVersion()
        );
    }
}
