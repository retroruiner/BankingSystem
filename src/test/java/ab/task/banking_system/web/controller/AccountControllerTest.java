package ab.task.banking_system.web.controller;

import ab.task.banking_system.model.Account;
import ab.task.banking_system.service.AccountService;
import ab.task.banking_system.web.dto.AccountCreateRequest;
import ab.task.banking_system.web.dto.AccountResponse;
import ab.task.banking_system.web.dto.AmountRequest;
import ab.task.banking_system.web.mapper.AccountMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AccountService accountService;

    @MockitoBean
    AccountMapper accountMapper;

    @Test
    void create_returns201_withBody() throws Exception {
        AccountCreateRequest req = new AccountCreateRequest(5L, "5f8d5v7f5d6c8d");

        Account saved = new Account();
        saved.setId(10L);
        saved.setNumber("5f8d5v7f5d6c8d");

        AccountResponse resp = new AccountResponse(10L, "5f8d5v7f5d6c8d", BigDecimal.ZERO, 5L);

        given(accountService.create(5L, "5f8d5v7f5d6c8d")).willReturn(saved);
        given(accountMapper.toResponse(saved)).willReturn(resp);

        mockMvc.perform(post("/api/accounts")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.userId").value(5))
                .andExpect(jsonPath("$.number").value("5f8d5v7f5d6c8d"));
    }

    @Test
    void create_withoutNumber_generated_returns201() throws Exception {
        String body = """
            {"userId": 7}
            """;

        Account saved = new Account();
        saved.setId(11L);
        saved.setNumber("9865qwert");

        AccountResponse resp = new AccountResponse(11L, "9865qwert", BigDecimal.ZERO, 7L);

        given(accountService.create(7L, null)).willReturn(saved);
        given(accountMapper.toResponse(saved)).willReturn(resp);

        mockMvc.perform(post("/api/accounts")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.userId").value(7))
                .andExpect(jsonPath("$.number").value("9865qwert"));
    }

    @Test
    void deposit_returns204() throws Exception {
        AmountRequest amount = new AmountRequest(new BigDecimal("50.00"));
        doNothing().when(accountService).deposit(10L, new BigDecimal("50.00"));

        mockMvc.perform(post("/api/accounts/10/deposit")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(amount)))
                .andExpect(status().isNoContent());
    }

    @Test
    void withdraw_returns204() throws Exception {
        AmountRequest amount = new AmountRequest(new BigDecimal("20.00"));
        doNothing().when(accountService).withdraw(10L, new BigDecimal("20.00"));

        mockMvc.perform(post("/api/accounts/10/withdraw")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(amount)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deposit_notFound_returns404() throws Exception {
        AmountRequest amount = new AmountRequest(new BigDecimal("15.00"));

        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"))
                .when(accountService)
                .deposit(eq(999L), eq(amount.amount()));

        mockMvc.perform(post("/api/accounts/{id}/deposit", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(amount)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deposit_validationError_returns400_whenBodyMissingAmount() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be > 0"))
                .when(accountService)
                .deposit(eq(10L), isNull());

        String badJson = "{}";

        mockMvc.perform(post("/api/accounts/{id}/deposit", 10L)
                        .contentType(APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void withdraw_validationError_returns400_whenNegativeAmount() throws Exception {
        AmountRequest badAmount = new AmountRequest(new BigDecimal("-1.00"));

        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be > 0"))
                .when(accountService)
                .withdraw(eq(10L), eq(new BigDecimal("-1.00")));

        mockMvc.perform(post("/api/accounts/10/withdraw")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badAmount)))
                .andExpect(status().isBadRequest());
    }
}
