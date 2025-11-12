package com.yourorg.banking.compliance.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class CaseNumberGeneratorService {
    
    private static final String PREFIX = "AML";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    public String generateCaseNumber() {
        String dateString = LocalDateTime.now().format(DATE_FORMATTER);
        String timestamp = String.valueOf(System.currentTimeMillis() % 10000);
        return String.format("%s-%s-%s", PREFIX, dateString, timestamp);
    }
    
    public String generateSarNumber() {
        String dateString = LocalDateTime.now().format(DATE_FORMATTER);
        String timestamp = String.valueOf(System.currentTimeMillis() % 10000);
        return String.format("SAR-%s-%s", dateString, timestamp);
    }
    
    public String generateCtrNumber() {
        String dateString = LocalDateTime.now().format(DATE_FORMATTER);
        String timestamp = String.valueOf(System.currentTimeMillis() % 10000);
        return String.format("CTR-%s-%s", dateString, timestamp);
    }
}

