package com.yourorg.banking.investment.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AccountNumberGeneratorService {
    
    private final AtomicLong accountCounter = new AtomicLong(1);
    
    public String generateAccountNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long counter = accountCounter.getAndIncrement();
        return String.format("INV%s%06d", timestamp, counter);
    }
}

