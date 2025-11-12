package com.yourorg.banking.investment.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class OrderNumberGeneratorService {
    
    private final AtomicLong orderCounter = new AtomicLong(1);
    
    public String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        long counter = orderCounter.getAndIncrement();
        return String.format("ORD%s%06d", timestamp, counter);
    }
}

