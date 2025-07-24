package com.example.accounting.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * This class provides sample API tests for the Small Business Accounting System.
 * These tests demonstrate how to interact with the various API endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
public class ApiTestSamples {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Sample test for user registration
     */
    @Test
    public void testRegisterUser() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("username", "testuser");
        request.put("email", "test@example.com");
        request.put("password", "password123");
        request.put("role", "OWNER");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    /**
     * Sample test for user login
     */
    @Test
    public void testLogin() throws Exception {
        // First register a user
        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("username", "loginuser");
        registerRequest.put("email", "login@example.com");
        registerRequest.put("password", "password123");
        registerRequest.put("role", "OWNER");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Then login with the registered user
        Map<String, Object> loginRequest = new HashMap<>();
        loginRequest.put("username", "loginuser");
        loginRequest.put("password", "password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("loginuser"));
    }

    /**
     * Sample test for creating an account
     */
    @Test
    public void testCreateAccount() throws Exception {
        // First register and login to get a token
        String token = registerAndLogin("accountuser", "account@example.com", "password123");

        // Create an account
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Test Account");
        request.put("type", "ASSET");
        request.put("initialBalance", 1000.00);

        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Account"))
                .andExpect(jsonPath("$.type").value("ASSET"))
                .andExpect(jsonPath("$.balance").value(1000.00));
    }

    /**
     * Sample test for creating a transaction
     */
    @Test
    public void testCreateTransaction() throws Exception {
        // First register and login to get a token
        String token = registerAndLogin("transactionuser", "transaction@example.com", "password123");

        // Create an account
        Map<String, Object> accountRequest = new HashMap<>();
        accountRequest.put("name", "Transaction Test Account");
        accountRequest.put("type", "ASSET");
        accountRequest.put("initialBalance", 5000.00);

        ResultActions accountResult = mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(accountRequest)))
                .andExpect(status().isCreated());

        // Extract account ID from response
        String accountResponse = accountResult.andReturn().getResponse().getContentAsString();
        Integer accountId = objectMapper.readTree(accountResponse).get("id").asInt();

        // Create a transaction
        Map<String, Object> transactionRequest = new HashMap<>();
        transactionRequest.put("accountId", accountId);
        transactionRequest.put("amount", 100.00);
        transactionRequest.put("type", "INCOME");
        transactionRequest.put("category", "Sales");
        transactionRequest.put("date", "2023-01-01T10:00:00");
        transactionRequest.put("description", "Test transaction");

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(transactionRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountId").value(accountId))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.category").value("Sales"));
    }

    /**
     * Sample test for creating a budget
     */
    @Test
    public void testCreateBudget() throws Exception {
        // First register and login to get a token
        String token = registerAndLogin("budgetuser", "budget@example.com", "password123");

        // Create a budget
        Map<String, Object> request = new HashMap<>();
        request.put("category", "Office Supplies");
        request.put("amount", 500.00);
        request.put("period", "MONTHLY");

        mockMvc.perform(post("/api/budgets")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value("Office Supplies"))
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.period").value("MONTHLY"));
    }

    /**
     * Sample test for creating an invoice
     */
    @Test
    public void testCreateInvoice() throws Exception {
        // First register and login to get a token
        String token = registerAndLogin("invoiceuser", "invoice@example.com", "password123");

        // Create an account
        Map<String, Object> accountRequest = new HashMap<>();
        accountRequest.put("name", "Invoice Test Account");
        accountRequest.put("type", "ASSET");
        accountRequest.put("initialBalance", 0.00);

        ResultActions accountResult = mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(accountRequest)))
                .andExpect(status().isCreated());

        // Extract account ID from response
        String accountResponse = accountResult.andReturn().getResponse().getContentAsString();
        Integer accountId = objectMapper.readTree(accountResponse).get("id").asInt();

        // Create an invoice
        Map<String, Object> invoiceRequest = new HashMap<>();
        invoiceRequest.put("clientName", "Test Client");
        invoiceRequest.put("clientEmail", "client@example.com");
        invoiceRequest.put("dueDate", "2023-02-01");
        invoiceRequest.put("accountId", accountId);
        
        // Create invoice items
        Map<String, Object> item1 = new HashMap<>();
        item1.put("description", "Consulting Services");
        item1.put("quantity", 10);
        item1.put("unitPrice", 100.00);
        
        Map<String, Object> item2 = new HashMap<>();
        item2.put("description", "Software License");
        item2.put("quantity", 1);
        item2.put("unitPrice", 500.00);
        
        invoiceRequest.put("items", new Object[]{item1, item2});

        mockMvc.perform(post("/api/invoices")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(invoiceRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientName").value("Test Client"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items.length()").value(2));
    }

    /**
     * Sample test for generating a balance sheet report
     */
    @Test
    public void testGenerateBalanceSheet() throws Exception {
        // First register and login to get a token
        String token = registerAndLogin("reportuser", "report@example.com", "password123");

        // Create an asset account
        Map<String, Object> assetRequest = new HashMap<>();
        assetRequest.put("name", "Checking Account");
        assetRequest.put("type", "ASSET");
        assetRequest.put("initialBalance", 10000.00);

        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(assetRequest)))
                .andExpect(status().isCreated());

        // Create a liability account
        Map<String, Object> liabilityRequest = new HashMap<>();
        liabilityRequest.put("name", "Credit Card");
        liabilityRequest.put("type", "LIABILITY");
        liabilityRequest.put("initialBalance", 2000.00);

        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token)
                .content(objectMapper.writeValueAsString(liabilityRequest)))
                .andExpect(status().isCreated());

        // Generate balance sheet
        mockMvc.perform(get("/api/reports/balance-sheet")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assets").isArray())
                .andExpect(jsonPath("$.liabilities").isArray())
                .andExpect(jsonPath("$.totalAssets").value(10000.00))
                .andExpect(jsonPath("$.totalLiabilities").value(2000.00))
                .andExpect(jsonPath("$.equity").value(8000.00));
    }

    /**
     * Helper method to register a user and login to get a JWT token
     */
    private String registerAndLogin(String username, String email, String password) throws Exception {
        // Register user
        Map<String, Object> registerRequest = new HashMap<>();
        registerRequest.put("username", username);
        registerRequest.put("email", email);
        registerRequest.put("password", password);
        registerRequest.put("role", "OWNER");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        // Login to get token
        Map<String, Object> loginRequest = new HashMap<>();
        loginRequest.put("username", username);
        loginRequest.put("password", password);

        ResultActions loginResult = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        // Extract token from response
        String loginResponse = loginResult.andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(loginResponse).get("token").asText();
    }
}