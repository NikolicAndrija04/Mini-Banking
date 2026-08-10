package com.minibanking.notification.notification.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minibanking.notification.common.error.ConflictException;
import com.minibanking.notification.common.error.ResourceNotFoundException;
import com.minibanking.notification.notification.api.CreateNotificationRequest;
import com.minibanking.notification.notification.api.NotificationResponse;
import com.minibanking.notification.notification.api.UpdateNotificationRequest;
import com.minibanking.notification.notification.domain.CustomerNotification;
import com.minibanking.notification.notification.domain.NotificationStatus;
import com.minibanking.notification.notification.domain.NotificationType;
import com.minibanking.notification.notification.repository.CustomerNotificationRepository;

@Service
public class NotificationService {

    private final CustomerNotificationRepository notificationRepository;

    public NotificationService(CustomerNotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public NotificationResponse create(CreateNotificationRequest request) {
        UUID eventId = request.eventId() == null ? UUID.randomUUID() : request.eventId();
        if (notificationRepository.existsByEventId(eventId)) {
            throw new ConflictException("A notification for event " + eventId + " already exists");
        }
        CustomerNotification notification = new CustomerNotification(
                eventId,
                request.customerId(),
                request.type(),
                request.title().trim(),
                request.message().trim()
        );
        return toResponse(notificationRepository.save(notification));
    }

    @Transactional(readOnly = true)
    public NotificationResponse findById(UUID id) {
        return toResponse(requireNotification(id));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> findAll(
            UUID customerId,
            NotificationType type,
            NotificationStatus status
    ) {
        return notificationRepository.findAll().stream()
                .filter(notification -> customerId == null || notification.getCustomerId().equals(customerId))
                .filter(notification -> type == null || notification.getType() == type)
                .filter(notification -> status == null || notification.getStatus() == status)
                .sorted(Comparator.comparing(CustomerNotification::getCreatedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NotificationResponse update(UUID id, UpdateNotificationRequest request) {
        CustomerNotification notification = requireNotification(id);
        notification.update(request.title().trim(), request.message().trim(), request.status());
        return toResponse(notification);
    }

    @Transactional
    public NotificationResponse markRead(UUID id) {
        CustomerNotification notification = requireNotification(id);
        notification.markRead();
        return toResponse(notification);
    }

    @Transactional
    public void delete(UUID id) {
        notificationRepository.delete(requireNotification(id));
    }

    @Transactional
    public Optional<NotificationResponse> createIfEventIsNew(CreateNotificationRequest request) {
        if (request.eventId() == null) {
            throw new IllegalArgumentException("An asynchronous notification must contain an event id");
        }
        if (notificationRepository.existsByEventId(request.eventId())) {
            return Optional.empty();
        }
        return Optional.of(create(request));
    }

    private CustomerNotification requireNotification(UUID id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification " + id + " was not found"));
    }

    private NotificationResponse toResponse(CustomerNotification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getEventId(),
                notification.getCustomerId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getStatus(),
                notification.getCreatedAt(),
                notification.getUpdatedAt(),
                notification.getReadAt()
        );
    }
}
