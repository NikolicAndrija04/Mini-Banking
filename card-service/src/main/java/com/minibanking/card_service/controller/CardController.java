package com.minibanking.card_service.controller;

import com.minibanking.card_service.dto.CardRequest;
import com.minibanking.card_service.dto.CardResponse;
import com.minibanking.card_service.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Cards",
        description = "Operations for managing bank cards"
)
@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @Operation(
            summary = "Create card",
            description = "Creates a card for an existing active account"
    )
    @ApiResponse(responseCode = "201", description = "Card created successfully")
    @ApiResponse(responseCode = "404", description = "Account not found")
    @ApiResponse(responseCode = "409", description = "Account is not active")
    @PostMapping
    public ResponseEntity<CardResponse> create(
            @Valid @RequestBody CardRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cardService.create(request));
    }

    @Operation(summary = "Get all cards")
    @GetMapping
    public ResponseEntity<List<CardResponse>> getAll() {
        return ResponseEntity.ok(cardService.getAll());
    }

    @Operation(
            summary = "Get card by ID",
            description = "Returns card data and resolves card holder information through Account and Customer services"
    )
    @GetMapping("/{id}")
    public ResponseEntity<CardResponse> getById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(cardService.getById(id));
    }

    @Operation(summary = "Get cards by account ID")
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<CardResponse>> getByAccountId(
            @PathVariable Long accountId
    ) {
        return ResponseEntity.ok(
                cardService.getByAccountId(accountId)
        );
    }

    @Operation(summary = "Block card")
    @PutMapping("/{id}/block")
    public ResponseEntity<CardResponse> block(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(cardService.block(id));
    }

    @Operation(summary = "Delete card")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id
    ) {
        cardService.delete(id);
        return ResponseEntity.noContent().build();
    }
}