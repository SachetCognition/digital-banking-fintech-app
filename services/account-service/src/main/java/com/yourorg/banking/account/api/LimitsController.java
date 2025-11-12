package com.digitalbank.fintech.account.api;

import com.digitalbank.fintech.account.model.AccountLimit;
import com.digitalbank.fintech.account.repo.LimitsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Account Limits")
@Validated
public class LimitsController {

    private final LimitsRepository limitsRepository;

    public LimitsController(LimitsRepository limitsRepository) {
        this.limitsRepository = limitsRepository;
    }

    @GetMapping("/{accountId}/limits")
    @Operation(summary = "Get limits for an account")
    public ResponseEntity<?> getLimits(@PathVariable UUID accountId) {
        Optional<AccountLimit> lim = limitsRepository.findByAccountId(accountId);
        return lim.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok(Map.of(
                        "accountId", accountId.toString(),
                        "limitType", "DEFAULT",
                        "dailyAmount", "0.00",
                        "perTxnAmount", "0.00"
                )));
    }

    @PutMapping("/{accountId}/limits")
    @Operation(summary = "Upsert limits for an account")
    public ResponseEntity<?> putLimits(
            @PathVariable UUID accountId,
            @RequestBody Map<String, Object> body
    ) {
        String limitType = String.valueOf(body.getOrDefault("limitType", "DEFAULT"));
        BigDecimal daily = new BigDecimal(String.valueOf(body.getOrDefault("dailyAmount", "0.00")));
        BigDecimal perTxn = new BigDecimal(String.valueOf(body.getOrDefault("perTxnAmount", "0.00")));
        AccountLimit updated = limitsRepository.upsert(accountId, limitType, daily, perTxn);
        return ResponseEntity.ok(updated);
    }
}
