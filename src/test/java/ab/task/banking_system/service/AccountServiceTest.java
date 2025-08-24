package ab.task.banking_system.service;

import ab.task.banking_system.model.Account;
import ab.task.banking_system.model.User;
import ab.task.banking_system.repository.AccountRepository;
import ab.task.banking_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock AccountRepository accountRepository;
    @Mock UserRepository userRepository;

    @InjectMocks AccountService accountService;

    User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Adilet");
        user.setEmail("adilet@example.com");
    }

    @Test
    void create_withProvidedNumber_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account a = inv.getArgument(0);
            a.setId(10L);
            return a;
        });

        Account saved = accountService.create(1L, "1a2d3e4");

        assertNotNull(saved);
        assertEquals(10L, saved.getId());
        assertEquals("1a2d3e4", saved.getNumber());
        verify(userRepository).findById(1L);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void create_withoutNumber_generatesNumber_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account a = inv.getArgument(0);
            a.setId(11L);
            return a;
        });

        Account saved = accountService.create(1L, null);

        verify(accountRepository).save(captor.capture());
        Account toSave = captor.getValue();
        assertNotNull(toSave.getNumber());
        assertFalse(toSave.getNumber().isBlank());
        assertEquals(saved.getNumber(), toSave.getNumber());
    }

    @Test
    void create_conflict_whenNumberDuplicate() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(accountRepository.save(any(Account.class)))
                .thenThrow(new DataIntegrityViolationException("unique"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> accountService.create(1L, "DUP"));

        assertEquals(CONFLICT.value(), ex.getStatusCode().value());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void listByUser_ok() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(accountRepository.findByUserId(1L)).thenReturn(List.of(new Account(), new Account()));

        var list = accountService.listByUser(1L);

        assertEquals(2, list.size());
        verify(accountRepository).findByUserId(1L);
    }

    @Test
    void listByUser_userNotFound_404() {
        when(userRepository.existsById(999L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> accountService.listByUser(999L));

        assertEquals(NOT_FOUND.value(), ex.getStatusCode().value());
    }

    @Test
    void deposit_success() {
        when(accountRepository.deposit(5L, new BigDecimal("10.00"))).thenReturn(1);

        accountService.deposit(5L, new BigDecimal("10.00"));

        verify(accountRepository).deposit(5L, new BigDecimal("10.00"));
    }

    @Test
    void deposit_accountNotFound_404() {
        when(accountRepository.deposit(5L, new BigDecimal("10.00"))).thenReturn(0);
        when(accountRepository.existsById(5L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> accountService.deposit(5L, new BigDecimal("10.00")));

        assertEquals(NOT_FOUND.value(), ex.getStatusCode().value());
    }

    @Test
    void deposit_failed_unknownConflict_409() {
        when(accountRepository.deposit(5L, new BigDecimal("10.00"))).thenReturn(0);
        when(accountRepository.existsById(5L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> accountService.deposit(5L, new BigDecimal("10.00")));

        assertEquals(CONFLICT.value(), ex.getStatusCode().value());
    }

    @Test
    void withdraw_success() {
        when(accountRepository.withdrawIfEnough(7L, new BigDecimal("4.50"))).thenReturn(1);

        accountService.withdraw(7L, new BigDecimal("4.50"));

        verify(accountRepository).withdrawIfEnough(7L, new BigDecimal("4.50"));
    }

    @Test
    void withdraw_accountNotFound_404() {
        when(accountRepository.withdrawIfEnough(7L, new BigDecimal("4.50"))).thenReturn(0);
        when(accountRepository.existsById(7L)).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> accountService.withdraw(7L, new BigDecimal("4.50")));

        assertEquals(NOT_FOUND.value(), ex.getStatusCode().value());
    }

    @Test
    void withdraw_insufficientFunds_409() {
        when(accountRepository.withdrawIfEnough(7L, new BigDecimal("100.00"))).thenReturn(0);
        when(accountRepository.existsById(7L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> accountService.withdraw(7L, new BigDecimal("100.00")));

        assertEquals(CONFLICT.value(), ex.getStatusCode().value());
        assertTrue(ex.getReason().toLowerCase().contains("insufficient"));
    }

    @Test
    void amountValidation_zeroOrNegative_400() {
        ResponseStatusException ex1 = assertThrows(ResponseStatusException.class,
                () -> accountService.deposit(1L, new BigDecimal("0.00")));
        assertEquals(BAD_REQUEST.value(), ex1.getStatusCode().value());

        ResponseStatusException ex2 = assertThrows(ResponseStatusException.class,
                () -> accountService.withdraw(1L, new BigDecimal("-1.00")));
        assertEquals(BAD_REQUEST.value(), ex2.getStatusCode().value());
    }
}
