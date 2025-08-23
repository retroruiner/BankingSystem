package ab.task.banking_system.service;

import ab.task.banking_system.model.Account;
import ab.task.banking_system.model.User;
import ab.task.banking_system.repository.AccountRepository;
import ab.task.banking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional
    public Account create(Long userId, String providedNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        String number = (providedNumber != null && !providedNumber.isBlank())
                ? providedNumber
                : generateAccountNumber();

        Account acc = new Account();
        acc.setUser(user);
        acc.setNumber(number);

        try {
            return accountRepository.save(acc);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account number already exists");
        }
    }

    private String generateAccountNumber() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    @Transactional(readOnly = true)
    public List<Account> listByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return accountRepository.findByUserId(userId);
    }

    @Transactional
    public void deposit(Long accountId, BigDecimal amount) {
        requirePositive(amount);
        int rows = accountRepository.deposit(accountId, amount);
        if (rows == 0) {
            if (!accountRepository.existsById(accountId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Deposit failed");
        }
    }

    @Transactional
    public void withdraw(Long accountId, BigDecimal amount) {
        requirePositive(amount);
        int rows = accountRepository.withdrawIfEnough(accountId, amount);
        if (rows == 0) {
            if (!accountRepository.existsById(accountId)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient funds");
        }
    }

    private static void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be > 0");
        }
    }
}
