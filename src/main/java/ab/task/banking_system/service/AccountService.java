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

    /**
     * Создаёт новый счёт для пользователя.
     *
     * @param userId идентификатор пользователя
     * @param providedNumber номер счёта, если задан; если пустой - будет сгенерирован
     * @return сохранённый счёт
     * @throws ResponseStatusException если пользователь не найден (404) или номер счёта уже существует (409)
     */
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
            Throwable cause = e.getMostSpecificCause();
            log.warn("Создание счёта: номер уже существует number={}, cause={}", number, cause.getMessage());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account number already exists");
        }
    }

    private String generateAccountNumber() {
        String n = UUID.randomUUID().toString().replace("-", "");
        log.debug("Сгенерирован номер счёта: {}", n);
        return n;
    }

    /**
     * Возвращает список счетов пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список счетов пользователя
     * @throws ResponseStatusException если пользователь не найден (404)
     */
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

    /**
     * Выполняет пополнение счёта.
     *
     * @param accountId идентификатор счёта
     * @param amount сумма пополнения (должна быть > 0)
     * @throws ResponseStatusException если сумма некорректна (400), счёт не найден (404) или пополнение не применено (409)
     */
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

    /**
     * Выполняет списание со счёта.
     *
     * @param accountId идентификатор счёта
     * @param amount сумма списания (должна быть > 0)
     * @throws ResponseStatusException если сумма некорректна (400), счёт не найден (404) или недостаточно средств (409)
     */
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
