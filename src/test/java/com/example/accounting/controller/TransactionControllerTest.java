package com.example.accounting.controller;

import com.example.accounting.dto.transaction.TransactionRequest;
import com.example.accounting.model.Account;
import com.example.accounting.model.Transaction;
import com.example.accounting.model.Transaction.TransactionType;
import com.example.accounting.model.User;
import com.example.accounting.repository.AccountRepository;
import com.example.accounting.repository.TransactionRepository;
import com.example.accounting.repository.UserRepository;
import com.example.accounting.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private Account testAccount;
    private String jwtToken;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @BeforeEach
    public void setup() {
        // Create a test user
        testUser = new User();
        testUser.setUsername("transactiontestuser");
        testUser.setEmail("transactiontest@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole(User.Role.OWNER);
        userRepository.save(testUser);

        // Create a test account
        testAccount = new Account();
        testAccount.setUser(testUser);
        testAccount.setName("Transaction Test Account");
        testAccount.setType(Account.AccountType.ASSET);
        testAccount.setBalance(new BigDecimal("5000.00"));
        accountRepository.save(testAccount);

        // Generate JWT token for the test user
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                testUser.getUsername(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + testUser.getRole().name()))
        );
        jwtToken = jwtTokenProvider.generateToken(authentication);
    }

    @Test
    public void testCreateTransaction() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        TransactionRequest request = new TransactionRequest();
        request.setAccountId(testAccount.getId());
        request.setAmount(new BigDecimal("100.00"));
        request.setType(TransactionType.INCOME);
        request.setCategory("Sales");
        request.setDate(now);
        request.setDescription("Test transaction");

        ResultActions result = mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.accountId", is(testAccount.getId().intValue())))
                .andExpect(jsonPath("$.amount", is(100.00)))
                .andExpect(jsonPath("$.type", is("INCOME")))
                .andExpect(jsonPath("$.category", is("Sales")))
                .andExpect(jsonPath("$.description", is("Test transaction")));

        // Verify account balance is updated
        mockMvc.perform(get("/api/accounts/" + testAccount.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", is(5100.00)));
    }

    @Test
    public void testGetTransaction() throws Exception {
        // Create a transaction first
        Transaction transaction = new Transaction();
        transaction.setAccount(testAccount);
        transaction.setAmount(new BigDecimal("200.00"));
        transaction.setType(TransactionType.EXPENSE);
        transaction.setCategory("Office Supplies");
        transaction.setDate(LocalDateTime.now());
        transaction.setDescription("Get test transaction");
        transactionRepository.save(transaction);

        // Update account balance
        testAccount.setBalance(testAccount.getBalance().subtract(transaction.getAmount()));
        accountRepository.save(testAccount);

        ResultActions result = mockMvc.perform(get("/api/transactions/" + transaction.getId())
                .header("Authorization", "Bearer " + jwtToken));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(transaction.getId().intValue())))
                .andExpect(jsonPath("$.accountId", is(testAccount.getId().intValue())))
                .andExpect(jsonPath("$.amount", is(200.00)))
                .andExpect(jsonPath("$.type", is("EXPENSE")))
                .andExpect(jsonPath("$.category", is("Office Supplies")))
                .andExpect(jsonPath("$.description", is("Get test transaction")));
    }

    @Test
    public void testGetAllTransactions() throws Exception {
        // Create multiple transactions
        Transaction transaction1 = new Transaction();
        transaction1.setAccount(testAccount);
        transaction1.setAmount(new BigDecimal("300.00"));
        transaction1.setType(TransactionType.INCOME);
        transaction1.setCategory("Consulting");
        transaction1.setDate(LocalDateTime.now());
        transaction1.setDescription("List test transaction 1");
        transactionRepository.save(transaction1);

        Transaction transaction2 = new Transaction();
        transaction2.setAccount(testAccount);
        transaction2.setAmount(new BigDecimal("150.00"));
        transaction2.setType(TransactionType.EXPENSE);
        transaction2.setCategory("Utilities");
        transaction2.setDate(LocalDateTime.now());
        transaction2.setDescription("List test transaction 2");
        transactionRepository.save(transaction2);

        // Update account balance
        testAccount.setBalance(testAccount.getBalance().add(transaction1.getAmount()).subtract(transaction2.getAmount()));
        accountRepository.save(testAccount);

        ResultActions result = mockMvc.perform(get("/api/transactions?accountId=" + testAccount.getId())
                .header("Authorization", "Bearer " + jwtToken));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$.content[*].description", hasItems("List test transaction 1", "List test transaction 2")));
    }

    @Test
    public void testUpdateTransaction() throws Exception {
        // Create a transaction first
        Transaction transaction = new Transaction();
        transaction.setAccount(testAccount);
        transaction.setAmount(new BigDecimal("400.00"));
        transaction.setType(TransactionType.INCOME);
        transaction.setCategory("Sales");
        transaction.setDate(LocalDateTime.now());
        transaction.setDescription("Update test transaction");
        transactionRepository.save(transaction);

        // Update account balance
        testAccount.setBalance(testAccount.getBalance().add(transaction.getAmount()));
        accountRepository.save(testAccount);

        // Update the transaction description and category
        TransactionRequest request = new TransactionRequest();
        request.setAccountId(testAccount.getId());
        request.setAmount(transaction.getAmount());
        request.setType(transaction.getType());
        request.setCategory("Updated Category");
        request.setDate(transaction.getDate());
        request.setDescription("Updated description");

        ResultActions result = mockMvc.perform(put("/api/transactions/" + transaction.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(transaction.getId().intValue())))
                .andExpect(jsonPath("$.accountId", is(testAccount.getId().intValue())))
                .andExpect(jsonPath("$.amount", is(400.00)))
                .andExpect(jsonPath("$.type", is("INCOME")))
                .andExpect(jsonPath("$.category", is("Updated Category")))
                .andExpect(jsonPath("$.description", is("Updated description")));
    }

    @Test
    public void testDeleteTransaction() throws Exception {
        // Create a transaction first
        Transaction transaction = new Transaction();
        transaction.setAccount(testAccount);
        transaction.setAmount(new BigDecimal("500.00"));
        transaction.setType(TransactionType.EXPENSE);
        transaction.setCategory("Travel");
        transaction.setDate(LocalDateTime.now());
        transaction.setDescription("Delete test transaction");
        transactionRepository.save(transaction);

        // Update account balance
        testAccount.setBalance(testAccount.getBalance().subtract(transaction.getAmount()));
        accountRepository.save(testAccount);

        // Get the initial balance
        BigDecimal initialBalance = testAccount.getBalance();

        ResultActions result = mockMvc.perform(delete("/api/transactions/" + transaction.getId())
                .header("Authorization", "Bearer " + jwtToken));

        result.andExpect(status().isOk());

        // Verify the transaction is deleted
        mockMvc.perform(get("/api/transactions/" + transaction.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());

        // Verify account balance is updated (expense removed, so balance should increase)
        mockMvc.perform(get("/api/accounts/" + testAccount.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", is(initialBalance.add(transaction.getAmount()).doubleValue())));
    }
}