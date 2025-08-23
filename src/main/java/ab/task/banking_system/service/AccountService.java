package ab.task.banking_system.service;

import ab.task.banking_system.model.Account;
import ab.task.banking_system.model.User;
import ab.task.banking_system.repository.AccountRepository;
import ab.task.banking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional
    public Account create(Long userId, String providedNumber) {
        log.info("Создание счёта: userId={}, providedNumber={}", userId, providedNumber != null && !providedNumber.isBlank());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Создание счёта: пользователь не найден userId={}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        String number = (providedNumber != null && !providedNumber.isBlank())
                ? providedNumber
                : generateAccountNumber();

        Account acc = new Account();
        acc.setUser(user);
        acc.setNumber(number);

        try {
            Account saved = accountRepository.save(acc);
            log.info("Счёт создан: id={}, number={}, userId={}", saved.getId(), saved.getNumber(), userId);
            return saved;
        } catch (DataIntegrityViolationException e) {
            var cause = e.getMostSpecificCause();
            log.warn("Создание счёта: номер уже существует number={}, cause={}", number, cause.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account number already exists");
        }
    }

    private String generateAccountNumber() {
        String n = UUID.randomUUID().toString().replace("-", "");
        log.debug("Сгенерирован номер счёта: {}", n);
        return n;
    }

    @Transactional(readOnly = true)
    public List<Account> listByUser(Long userId) {
        log.info("Запрос счетов пользователя: userId={}", userId);
        if (!userRepository.existsById(userId)) {
            log.warn("Счета пользователя: пользователь не найден userId={}", userId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        List<Account> list = accountRepository.findByUserId(userId);
        log.info("Счета пользователя: userId={}, count={}", userId, list.size());
        return list;
    }

    @Transactional
    public void deposit(Long accountId, BigDecimal amount) {
        log.info("Депозит: accountId={}, amount={}", accountId, amount);
        requirePositive(amount);
        int rows = accountRepository.deposit(accountId, amount);
        if (rows == 0) {
            if (!accountRepository.existsById(accountId)) {
                log.warn("Депозит: счёт не найден accountId={}", accountId);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
            }
            log.warn("Депозит не выполнен (неизвестная причина): accountId={}, amount={}", accountId, amount);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Deposit failed");
        }
        log.info("Депозит выполнен: accountId={}, amount={}", accountId, amount);
    }

    @Transactional
    public void withdraw(Long accountId, BigDecimal amount) {
        log.info("Списание: accountId={}, amount={}", accountId, amount);
        requirePositive(amount);
        int rows = accountRepository.withdrawIfEnough(accountId, amount);
        if (rows == 0) {
            if (!accountRepository.existsById(accountId)) {
                log.warn("Списание: счёт не найден accountId={}", accountId);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found");
            }
            log.warn("Списание отклонено: недостаточно средств accountId={}, amount={}", accountId, amount);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient funds");
        }
        log.info("Списание выполнено: accountId={}, amount={}", accountId, amount);
    }

    private void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            log.warn("Некорректная сумма операции: {}", amount);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be > 0");
        }
    }
}
