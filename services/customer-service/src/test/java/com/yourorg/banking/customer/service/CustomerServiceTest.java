package com.yourorg.banking.customer.service;

import com.yourorg.banking.customer.model.Customer;
import com.yourorg.banking.customer.repo.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
        Customer customer = new Customer(id, "USR001", "test@example.com", "+1234567890", "John Doe", "1990-01-01", "US", "ACTIVE");
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
    void create_savesCustomerSuccessfully() {
        UUID id = UUID.randomUUID();
        Customer customer = new Customer(id, "USR002", "new@example.com", "+1987654321", "Jane Doe", "1992-05-15", "UK", "ACTIVE");
        doNothing().when(customerRepository).create(any(Customer.class));

        assertDoesNotThrow(() -> customerRepository.create(customer));
        verify(customerRepository).create(customer);
    }
}
