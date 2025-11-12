package com.yourorg.banking.investment.api;

import com.yourorg.banking.investment.model.RiskTolerance;
import com.yourorg.banking.investment.model.dto.AccountResponse;
import com.yourorg.banking.investment.model.dto.CreateAccountRequest;
import com.yourorg.banking.investment.service.InvestmentAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/investment-accounts")
@RequiredArgsConstructor
@Slf4j
public class InvestmentAccountController {
    
    private final InvestmentAccountService investmentAccountService;
    
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @AuthenticationPrincipal String customerId,
            @RequestBody CreateAccountRequest request) {
        log.info("Creating investment account for customer: {}", customerId);
        
        UUID customerUuid = UUID.fromString(customerId);
        AccountResponse response = investmentAccountService.createAccount(customerUuid, request);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getCustomerAccounts(
            @AuthenticationPrincipal String customerId) {
        log.info("Getting investment accounts for customer: {}", customerId);
        
        UUID customerUuid = UUID.fromString(customerId);
        List<AccountResponse> accounts = investmentAccountService.getCustomerAccounts(customerUuid);
        
        return ResponseEntity.ok(accounts);
    }
    
    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber) {
        log.info("Getting investment account: {}", accountNumber);
        
        AccountResponse account = investmentAccountService.getAccount(accountNumber);
        
        return ResponseEntity.ok(account);
    }
    
    @PutMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable String accountNumber,
            @RequestParam(required = false) String accountName,
            @RequestParam(required = false) RiskTolerance riskTolerance,
            @RequestParam(required = false) String investmentObjective) {
        log.info("Updating investment account: {}", accountNumber);
        
        AccountResponse response = investmentAccountService.updateAccount(
            accountNumber, accountName, riskTolerance, investmentObjective);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<AccountResponse> deposit(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount) {
        log.info("Depositing {} to account: {}", amount, accountNumber);
        
        AccountResponse response = investmentAccountService.deposit(accountNumber, amount);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<AccountResponse> withdraw(
            @PathVariable String accountNumber,
            @RequestParam BigDecimal amount) {
        log.info("Withdrawing {} from account: {}", amount, accountNumber);
        
        AccountResponse response = investmentAccountService.withdraw(accountNumber, amount);
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{accountNumber}/close")
    public ResponseEntity<AccountResponse> closeAccount(@PathVariable String accountNumber) {
        log.info("Closing investment account: {}", accountNumber);
        
        AccountResponse response = investmentAccountService.closeAccount(accountNumber);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Investment Account Service is healthy");
    }
}

