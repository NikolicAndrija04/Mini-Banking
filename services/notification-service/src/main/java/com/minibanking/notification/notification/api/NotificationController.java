package com.minibanking.notification.notification.api;

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

import com.minibanking.notification.notification.domain.NotificationStatus;
import com.minibanking.notification.notification.domain.NotificationType;
import com.minibanking.notification.notification.service.NotificationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Persistent in-app customer notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    @Operation(summary = "Create a notification")
    @ApiResponse(responseCode = "201", description = "Notification created")
    public ResponseEntity<NotificationResponse> create(@Valid @RequestBody CreateNotificationRequest request) {
        NotificationResponse created = notificationService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a notification by id")
    public NotificationResponse findById(@PathVariable UUID id) {
        return notificationService.findById(id);
    }

    @GetMapping
    @Operation(summary = "List notifications with optional filters")
    public List<NotificationResponse> findAll(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) NotificationStatus status
    ) {
        return notificationService.findAll(customerId, type, status);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Replace editable notification data")
    public NotificationResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateNotificationRequest request
    ) {
        return notificationService.update(id, request);
    }

    @PostMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public NotificationResponse markRead(@PathVariable UUID id) {
        return notificationService.markRead(id);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a notification")
    @ApiResponse(responseCode = "204", description = "Notification deleted")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
