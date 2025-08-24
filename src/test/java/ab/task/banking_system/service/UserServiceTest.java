package ab.task.banking_system.service;

import ab.task.banking_system.model.User;
import ab.task.banking_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService userService;

    User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Adilet");
        user.setEmail("adilet@example.com");
    }

    @Test
    void create_success() {
        when(userRepository.existsByEmail("adilet@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User saved = userService.create("Adilet", "adilet@example.com");

        assertNotNull(saved);
        assertEquals(1L, saved.getId());
        assertEquals("adilet@example.com", saved.getEmail());
        verify(userRepository).existsByEmail("adilet@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void create_conflict_whenEmailAlreadyExists_precheck() {
        when(userRepository.existsByEmail("vincent@example.com")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.create("Vincent", "vincent@example.com"));

        assertEquals(CONFLICT.value(), ex.getStatusCode().value());
        verify(userRepository).existsByEmail("vincent@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void create_conflict_whenDbUniqueViolation_race() {
        when(userRepository.existsByEmail("peter@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("unique_violation"));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.create("Peter", "peter@example.com"));

        assertEquals(CONFLICT.value(), ex.getStatusCode().value());
        verify(userRepository).existsByEmail("peter@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getById_found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User found = userService.getById(1L);

        assertEquals(1L, found.getId());
        verify(userRepository).findById(1L);
    }

    @Test
    void getById_notFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> userService.getById(999L));

        assertEquals(NOT_FOUND.value(), ex.getStatusCode().value());
        verify(userRepository).findById(999L);
    }

    @Test
    void listAll_returnsUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> list = userService.listAll();

        assertEquals(1, list.size());
        verify(userRepository).findAll();
    }
}
