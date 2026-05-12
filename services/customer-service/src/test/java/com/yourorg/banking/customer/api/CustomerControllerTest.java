package com.yourorg.banking.customer.api;

import com.digitalbank.fintech.customer.api.CustomerController;
import com.digitalbank.fintech.customer.repo.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.bean.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerRepository customerRepository;

    @Test
    @WithMockUser
    void healthEndpoint_returns200() throws Exception {
        mockMvc.perform(get("/api/v1/customers/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ok"));
    }

    @Test
    @WithMockUser
    void getById_returnsNotFoundWhenMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(customerRepository.findById(any())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/customers/" + id))
            .andExpect(status().isNotFound());
    }
}
