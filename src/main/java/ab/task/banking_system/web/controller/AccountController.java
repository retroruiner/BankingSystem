package ab.task.banking_system.web.controller;

import ab.task.banking_system.service.AccountService;
import ab.task.banking_system.web.dto.AccountCreateRequest;
import ab.task.banking_system.web.dto.AccountResponse;
import ab.task.banking_system.web.mapper.AccountMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody AccountCreateRequest req) {
        var acc = accountService.create(req.userId(), req.number());
        return ResponseEntity.status(HttpStatus.CREATED).body(accountMapper.toResponse(acc));
    }
}
