package ab.task.banking_system.web.controller;

import ab.task.banking_system.service.AccountService;
import ab.task.banking_system.service.UserService;
import ab.task.banking_system.web.dto.AccountResponse;
import ab.task.banking_system.web.dto.UserCreateRequest;
import ab.task.banking_system.web.dto.UserResponse;
import ab.task.banking_system.web.mapper.AccountMapper;
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
    private final AccountService accountService;
    private final AccountMapper accountMapper;

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

    @GetMapping("/{id}/accounts")
    public List<AccountResponse> userAccounts(@PathVariable Long id) {
        return accountMapper.toResponse(accountService.listByUser(id));
    }
}
