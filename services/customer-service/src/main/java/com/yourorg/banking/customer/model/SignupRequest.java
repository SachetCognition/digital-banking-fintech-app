package com.yourorg.banking.customer.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public record SignupRequest(
        @Email(message = "Invalid email format") String email,
        @Pattern(regexp = "^[+0-9]{6,20}$", message = "Invalid phone number") String phone,
        @NotBlank(message = "Full name is required") String fullName,
        LocalDate dob,
        @Pattern(regexp = "^[A-Z]{2}$", message = "Country must be ISO-2 code") String country
) {}
