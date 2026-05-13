package com.yourorg.banking.account.service;

import com.yourorg.banking.account.model.AccountType;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class AccountNumberGenerator {

    private final Random random = new Random();

    public String generateAccountNumber(AccountType accountType) {
        String prefix = getAccountPrefix(accountType);
        String randomPart = generateRandomPart();
        String checksum = generateChecksum(prefix + randomPart);
        
        return prefix + randomPart + checksum;
    }

    private String getAccountPrefix(AccountType accountType) {
        return switch (accountType) {
            case CHECKING -> "10";
            case SAVINGS -> "20";
            case MONEY_MARKET -> "30";
            case CD -> "40";
            case BUSINESS_CHECKING -> "50";
            case BUSINESS_SAVINGS -> "60";
            case MERCHANT -> "70";
            case INVESTMENT -> "80";
            case IRA -> "90";
            case ROTH_IRA -> "91";
            case CREDIT_CARD -> "11";
            case LINE_OF_CREDIT -> "12";
            case PERSONAL_LOAN -> "13";
            case BUSINESS_LOAN -> "14";
            case MORTGAGE -> "15";
            case STUDENT -> "21";
            case SENIOR -> "22";
            case JOINT -> "23";
            case TRUST -> "24";
            case ESCROW -> "25";
            case WADIAH_CURRENT -> "31";
            case MUDARABAH_SAVINGS -> "32";
            case WAKALA_INVESTMENT -> "33";
            case COMMODITY_MURABAHA_DEPOSIT -> "34";
        };
    }

    private String generateRandomPart() {
        // Generate 8-digit random number
        int randomNum = random.nextInt(90000000) + 10000000;
        return String.format("%08d", randomNum);
    }

    private String generateChecksum(String accountNumber) {
        // Simple checksum algorithm (Luhn algorithm would be better for production)
        int sum = 0;
        for (char c : accountNumber.toCharArray()) {
            sum += Character.getNumericValue(c);
        }
        return String.valueOf(sum % 10);
    }
}

