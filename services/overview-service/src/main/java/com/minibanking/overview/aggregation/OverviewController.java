package com.minibanking.overview.aggregation;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/overview")
@Tag(name = "Overview", description = "Resilient multi-service banking views")
public class OverviewController {

    private final OverviewService overviewService;

    public OverviewController(OverviewService overviewService) {
        this.overviewService = overviewService;
    }

    @GetMapping("/customers/{customerId}")
    @Operation(
            summary = "Aggregate a complete customer banking view",
            description = "Combines customer, accounts, cards and transactions; unavailable services are reported as degraded."
    )
    public CustomerOverviewResponse customerOverview(@PathVariable UUID customerId) {
        return overviewService.customerOverview(customerId);
    }

    @GetMapping("/accounts/{accountId}")
    @Operation(summary = "Aggregate account, cards and transaction history")
    public AccountOverviewResponse accountOverview(@PathVariable UUID accountId) {
        return overviewService.accountOverview(accountId);
    }

    @GetMapping("/instance")
    @Operation(summary = "Show the serving instance for load-balancing demonstration")
    public InstanceResponse instance() {
        return overviewService.instance();
    }
}
