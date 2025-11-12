package com.digitalbank.fintech.customer.api;

import com.digitalbank.fintech.customer.model.Customer;
import com.digitalbank.fintech.customer.model.SignupRequest;
import com.digitalbank.fintech.customer.repo.CustomerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customers")
public class CustomerController {

    private final CustomerRepository repository;

    public CustomerController(CustomerRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/health")
    @Operation(summary = "Health check for customer-service")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @PostMapping("/signup")
    @Operation(summary = "Signup new customer (MVP)")
    public ResponseEntity<?> signup(@Validated @RequestBody SignupRequest req) {
        UUID id = UUID.randomUUID();
        String userNo = repository.generateUserNo();
        Customer c = new Customer(
                id,
                userNo,
                req.email(),
                req.phone(),
                req.fullName(),
                req.dob(),
                req.country(),
                "ACTIVE"
        );
        repository.create(c);
        return ResponseEntity.created(URI.create("/api/v1/customers/" + id))
                .body(Map.of(
                        "id", id.toString(),
                        "user_no", userNo
                ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<?> getById(@PathVariable("id") UUID id) {
        Optional<Customer> found = repository.findById(id);
        return found.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
