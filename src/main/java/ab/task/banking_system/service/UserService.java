package ab.task.banking_system.service;

import ab.task.banking_system.model.User;
import ab.task.banking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public User create(String name, String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(CONFLICT, "Email already exists");
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(CONFLICT, "Email already exists");
        }
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Transactional(readOnly = true)
    public List<User> listAll() {
        return userRepository.findAll();
    }
}
