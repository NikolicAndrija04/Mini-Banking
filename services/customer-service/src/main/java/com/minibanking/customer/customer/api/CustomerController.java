package com.minibanking.customer.customer.api;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.minibanking.customer.customer.service.CustomerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;

@Validated
@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "Customer profile and lifecycle operations")
public class CustomerController {

    private final CustomerService customerService;
    private final String welcomeMessage;
    private final String configurationSource;

    public CustomerController(
            CustomerService customerService,
            @Value("${minibanking.customer.welcome-message}") String welcomeMessage,
            @Value("${minibanking.platform.configuration-source:local-application-yml}") String configurationSource
    ) {
        this.customerService = customerService;
        this.welcomeMessage = welcomeMessage;
        this.configurationSource = configurationSource;
    }

    @PostMapping
    @Operation(summary = "Create a customer")
    @ApiResponse(responseCode = "201", description = "Customer created")
    @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content)
    @ApiResponse(responseCode = "409", description = "Email already exists", content = @Content)
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse created = customerService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a customer by id")
    @ApiResponse(responseCode = "200", description = "Customer found")
    @ApiResponse(responseCode = "404", description = "Customer not found", content = @Content)
    public CustomerResponse findById(@PathVariable UUID id) {
        return customerService.findById(id);
    }

    @GetMapping(params = "email")
    @Operation(summary = "Get a customer by email")
    public CustomerResponse findByEmail(@RequestParam @Email String email) {
        return customerService.findByEmail(email);
    }

    @GetMapping
    @Operation(summary = "List all customers")
    public List<CustomerResponse> findAll() {
        return customerService.findAll();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace customer data")
    public CustomerResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerRequest request
    ) {
        return customerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a customer")
    @ApiResponse(responseCode = "204", description = "Customer deleted")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/welcome")
    @Operation(
            summary = "Show the central configuration value",
            description = "Used during the defense to prove that this service reads a value from Config Server"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Configuration value",
            content = @Content(schema = @Schema(implementation = CustomerWelcomeResponse.class))
    )
    public CustomerWelcomeResponse welcome() {
        return new CustomerWelcomeResponse("customer-service", welcomeMessage, configurationSource);
    }
}
