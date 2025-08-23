package ab.task.banking_system.web;

import ab.task.banking_system.service.UserService;
import ab.task.banking_system.web.dto.UserCreateRequest;
import ab.task.banking_system.web.dto.UserResponse;
import ab.task.banking_system.web.mapper.UserMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest req) {
        var saved = userService.create(req.name(), req.email());
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(saved));
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        return userMapper.toResponse(userService.getById(id));
    }

    @GetMapping
    public List<UserResponse> list() {
        return userMapper.toResponse(userService.listAll());
    }
}
