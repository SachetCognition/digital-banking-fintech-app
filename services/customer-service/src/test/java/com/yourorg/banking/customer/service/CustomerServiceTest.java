package com.yourorg.banking.customer.service;

import com.yourorg.banking.customer.model.Customer;
import com.yourorg.banking.customer.model.CustomerStatus;
import com.yourorg.banking.customer.repo.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Test
    void findById_returnsCustomerWhenExists() {
        UUID id = UUID.randomUUID();
        Customer customer = new Customer(id, "USR001", "test@example.com", "+1234567890",
                "John Doe", LocalDate.of(1990, 1, 1), "US", CustomerStatus.ACTIVE,
                null, false, false, false, null, 0, null, Instant.now(), Instant.now());
        when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

        Optional<Customer> result = customerRepository.findById(id);

        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().email());
    }

    @Test
    void findById_returnsEmptyWhenNotExists() {
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Customer> result = customerRepository.findById(id);

        assertFalse(result.isPresent());
    }

    @Test
    void save_savesCustomerSuccessfully() {
        UUID id = UUID.randomUUID();
        Customer customer = new Customer(id, "USR002", "new@example.com", "+1987654321",
                "Jane Doe", LocalDate.of(1992, 5, 15), "UK", CustomerStatus.ACTIVE,
                null, false, false, false, null, 0, null, Instant.now(), Instant.now());
        doNothing().when(customerRepository).save(any(Customer.class));

        assertDoesNotThrow(() -> customerRepository.save(customer));
        verify(customerRepository).save(customer);
    }
}
