package ab.task.banking_system.service;

import ab.task.banking_system.model.User;
import ab.task.banking_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CONFLICT;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepo;

    @Transactional
    public User create(String name, String email) {
        if (userRepo.existsByEmail(email)) {
            throw new ResponseStatusException(CONFLICT, "Email already exists");
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        try {
            return userRepo.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(CONFLICT, "Email already exists");
        }
    }
}
