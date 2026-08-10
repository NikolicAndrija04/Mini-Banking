package com.minibanking.card.card.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.minibanking.card.card.domain.PaymentCard;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, UUID> {
}
