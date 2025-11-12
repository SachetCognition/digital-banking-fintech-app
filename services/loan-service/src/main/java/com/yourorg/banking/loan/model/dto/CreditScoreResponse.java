package com.yourorg.banking.loan.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditScoreResponse {
    private UUID id;
    private Integer score;
    private String scoreType;
    private String riskLevel;
    private String factors; // JSON string
    private LocalDateTime calculatedAt;
    private LocalDateTime expiresAt;
    private Boolean isExpired;
    private Boolean isGoodScore;
    private Boolean isExcellentScore;
}

