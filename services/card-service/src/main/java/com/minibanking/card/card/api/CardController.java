package com.minibanking.card.card.api;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
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

import com.minibanking.card.card.domain.CardStatus;
import com.minibanking.card.card.service.CardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/cards")
@Tag(name = "Cards", description = "Tokenized payment card lifecycle operations")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping
    @Operation(
            summary = "Issue a payment card",
            description = "Validates account ownership and status through account-service before issuance."
    )
    @ApiResponse(responseCode = "201", description = "Card issued")
    public ResponseEntity<CardResponse> create(@Valid @RequestBody CreateCardRequest request) {
        CardResponse created = cardService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a card by id")
    public CardResponse findById(@PathVariable UUID id) {
        return cardService.findById(id);
    }

    @GetMapping
    @Operation(summary = "List cards with optional filters")
    public List<CardResponse> findAll(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) CardStatus status
    ) {
        return cardService.findAll(customerId, accountId, status);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace editable card settings")
    public CardResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateCardRequest request) {
        return cardService.update(id, request);
    }

    @PostMapping("/{id}/block")
    @Operation(summary = "Block a card")
    public CardResponse block(@PathVariable UUID id) {
        return cardService.block(id);
    }

    @PostMapping("/{id}/activate")
    @Operation(summary = "Activate a card after revalidating its account")
    public CardResponse activate(@PathVariable UUID id) {
        return cardService.activate(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a blocked or expired card")
    @ApiResponse(responseCode = "204", description = "Card deleted")
    @ApiResponse(responseCode = "409", description = "Active card cannot be deleted", content = @Content)
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        cardService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
