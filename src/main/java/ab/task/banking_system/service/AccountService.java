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
}
