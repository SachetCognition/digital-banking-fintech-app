package com.yourorg.banking.card.repository;

import com.yourorg.banking.card.model.CardEvent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CardEventRepository extends CrudRepository<CardEvent, UUID> {
}

