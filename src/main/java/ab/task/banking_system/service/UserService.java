package ab.task.banking_system.service;

import ab.task.banking_system.model.User;
import ab.task.banking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public User create(String name, String email) {
        log.info("Создаю пользователя: name={}, email={}", name, email);

        if (userRepository.existsByEmail(email)) {
            log.warn("Не создаю - email уже занят: {}", email);
            throw new ResponseStatusException(CONFLICT, "Email already exists");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);

        try {
            User saved = userRepository.save(user);
            log.info("Пользователь создан: id={}, email={}", saved.getId(), saved.getEmail());
            return saved;
        } catch (DataIntegrityViolationException e) {
            log.warn("Конфликт при сохранении - email уже существует: {}", email);
            throw new ResponseStatusException(CONFLICT, "Email already exists");
        }
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        log.info("Читаю пользователя: id={}", id);
        return userRepository.findById(id)
                .map(u -> {
                    log.info("Нашёл пользователя: id={}", id);
                    return u;
                })
                .orElseThrow(() -> {
                    log.warn("Пользователь не найден: id={}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });
    }

    @Transactional(readOnly = true)
    public List<User> listAll() {
        log.info("Получаю список пользователей");
        List<User> users = userRepository.findAll();
        log.info("Всего пользователей: {}", users.size());
        return users;
    }
}
