package ab.task.banking_system.web.controller;

import ab.task.banking_system.model.Account;
import ab.task.banking_system.service.AccountService;
import ab.task.banking_system.web.dto.AccountCreateRequest;
import ab.task.banking_system.web.dto.AccountResponse;
import ab.task.banking_system.web.dto.AmountRequest;
import ab.task.banking_system.web.mapper.AccountMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
@Tag(name = "Accounts", description = "Операции со счетами")
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @Operation(
            summary = "Создать счёт",
            description = "Создаёт счёт для пользователя. Если номер не передан, генерируется автоматически.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountCreateRequest.class)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Счёт создан",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден"),
            @ApiResponse(responseCode = "409", description = "Номер счёта уже существует")
    })
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody AccountCreateRequest req) {
        Account acc = accountService.create(req.userId(), req.number());
        return ResponseEntity.status(HttpStatus.CREATED).body(accountMapper.toResponse(acc));
    }

    @Operation(
            summary = "Пополнить счёт",
            description = "Увеличивает баланс счёта на положительную сумму.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AmountRequest.class)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Баланс пополнен"),
            @ApiResponse(responseCode = "400", description = "Сумма должна быть > 0"),
            @ApiResponse(responseCode = "404", description = "Счёт не найден"),
            @ApiResponse(responseCode = "409", description = "Операция отклонена")
    })
    @PostMapping(value = "/{id}/deposit", consumes = "application/json")
    public ResponseEntity<Void> deposit(
            @Parameter(description = "ID счёта", required = true) @PathVariable Long id,
            @Valid @RequestBody AmountRequest req
    ) {
        accountService.deposit(id, req.amount());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Списать со счёта",
            description = "Уменьшает баланс счёта на положительную сумму при наличии достаточных средств.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AmountRequest.class)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Списание выполнено"),
            @ApiResponse(responseCode = "400", description = "Сумма должна быть > 0"),
            @ApiResponse(responseCode = "404", description = "Счёт не найден"),
            @ApiResponse(responseCode = "409", description = "Недостаточно средств")
    })
    @PostMapping(value = "/{id}/withdraw", consumes = "application/json")
    public ResponseEntity<Void> withdraw(
            @Parameter(description = "ID счёта", required = true) @PathVariable Long id,
            @Valid @RequestBody AmountRequest req
    ) {
        accountService.withdraw(id, req.amount());
        return ResponseEntity.noContent().build();
    }
}
