package com.example.accounting.controller;

import com.example.accounting.dto.account.AccountRequest;
import com.example.accounting.model.Account;
import com.example.accounting.model.User;
import com.example.accounting.repository.AccountRepository;
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
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private User testUser;
    private String jwtToken;

    @BeforeEach
    public void setup() {
        // Create a test user
        testUser = new User();
        testUser.setUsername("accounttestuser");
        testUser.setEmail("accounttest@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setRole(User.Role.OWNER);
        userRepository.save(testUser);

        // Generate JWT token for the test user
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                testUser.getUsername(),
                null,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + testUser.getRole().name()))
        );
        jwtToken = jwtTokenProvider.generateToken(authentication);
    }

    @Test
    public void testCreateAccount() throws Exception {
        AccountRequest request = new AccountRequest();
        request.setName("Test Account");
        request.setType(Account.AccountType.ASSET);
        request.setInitialBalance(new BigDecimal("1000.00"));

        ResultActions result = mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Test Account")))
                .andExpect(jsonPath("$.type", is("ASSET")))
                .andExpect(jsonPath("$.balance", is(1000.00)));
    }

    @Test
    public void testGetAccount() throws Exception {
        // Create an account first
        Account account = new Account();
        account.setUser(testUser);
        account.setName("Get Test Account");
        account.setType(Account.AccountType.ASSET);
        account.setBalance(new BigDecimal("2000.00"));
        accountRepository.save(account);

        ResultActions result = mockMvc.perform(get("/api/accounts/" + account.getId())
                .header("Authorization", "Bearer " + jwtToken));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(account.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Get Test Account")))
                .andExpect(jsonPath("$.type", is("ASSET")))
                .andExpect(jsonPath("$.balance", is(2000.00)));
    }

    @Test
    public void testGetAllAccounts() throws Exception {
        // Create multiple accounts
        Account account1 = new Account();
        account1.setUser(testUser);
        account1.setName("List Test Account 1");
        account1.setType(Account.AccountType.ASSET);
        account1.setBalance(new BigDecimal("1000.00"));
        accountRepository.save(account1);

        Account account2 = new Account();
        account2.setUser(testUser);
        account2.setName("List Test Account 2");
        account2.setType(Account.AccountType.LIABILITY);
        account2.setBalance(new BigDecimal("500.00"));
        accountRepository.save(account2);

        ResultActions result = mockMvc.perform(get("/api/accounts")
                .header("Authorization", "Bearer " + jwtToken));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[*].name", hasItems("List Test Account 1", "List Test Account 2")));
    }

    @Test
    public void testUpdateAccount() throws Exception {
        // Create an account first
        Account account = new Account();
        account.setUser(testUser);
        account.setName("Update Test Account");
        account.setType(Account.AccountType.ASSET);
        account.setBalance(new BigDecimal("3000.00"));
        accountRepository.save(account);

        // Update the account name
        AccountRequest request = new AccountRequest();
        request.setName("Updated Account Name");
        request.setType(Account.AccountType.ASSET);
        request.setInitialBalance(new BigDecimal("3000.00"));

        ResultActions result = mockMvc.perform(put("/api/accounts/" + account.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + jwtToken)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(account.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Updated Account Name")))
                .andExpect(jsonPath("$.type", is("ASSET")))
                .andExpect(jsonPath("$.balance", is(3000.00)));
    }

    @Test
    public void testDeleteAccount() throws Exception {
        // Create an account first
        Account account = new Account();
        account.setUser(testUser);
        account.setName("Delete Test Account");
        account.setType(Account.AccountType.ASSET);
        account.setBalance(new BigDecimal("4000.00"));
        accountRepository.save(account);

        ResultActions result = mockMvc.perform(delete("/api/accounts/" + account.getId())
                .header("Authorization", "Bearer " + jwtToken));

        result.andExpect(status().isOk());

        // Verify the account is deleted
        mockMvc.perform(get("/api/accounts/" + account.getId())
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }
}