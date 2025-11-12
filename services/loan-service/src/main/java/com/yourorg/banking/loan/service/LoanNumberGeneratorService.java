package com.yourorg.banking.loan.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class LoanNumberGeneratorService {
    
    private final AtomicLong applicationCounter = new AtomicLong(1);
    private final AtomicLong loanCounter = new AtomicLong(1);
    private final AtomicLong creditLineCounter = new AtomicLong(1);
    
    public String generateApplicationNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long counter = applicationCounter.getAndIncrement();
        return String.format("APP%s%06d", timestamp, counter);
    }
    
    public String generateLoanNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long counter = loanCounter.getAndIncrement();
        return String.format("LOAN%s%06d", timestamp, counter);
    }
    
    public String generateCreditLineNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long counter = creditLineCounter.getAndIncrement();
        return String.format("CL%s%06d", timestamp, counter);
    }
}

