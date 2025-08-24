package ab.task.banking_system.web.controller;

import ab.task.banking_system.service.AccountService;
import ab.task.banking_system.service.UserService;
import ab.task.banking_system.web.dto.AccountResponse;
import ab.task.banking_system.web.dto.UserCreateRequest;
import ab.task.banking_system.web.dto.UserResponse;
import ab.task.banking_system.web.mapper.AccountMapper;
import ab.task.banking_system.web.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Операции с пользователями")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @Operation(
            summary = "Создать пользователя",
            description = "Создаёт нового пользователя. Email должен быть уникальным.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserCreateRequest.class)
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Пользователь создан",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации"),
            @ApiResponse(responseCode = "409", description = "Email уже существует")
    })
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest req) {
        UserResponse body = userMapper.toResponse(userService.create(req.name(), req.email()));
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @Operation(
            summary = "Получить пользователя по ID",
            description = "Возвращает данные пользователя по его идентификатору."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "ОК",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping(value = "/{id}", produces = "application/json")
    public UserResponse get(
            @Parameter(description = "ID пользователя", required = true) @PathVariable Long id
    ) {
        return userMapper.toResponse(userService.getById(id));
    }

    @Operation(
            summary = "Список пользователей",
            description = "Возвращает список всех пользователей."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "ОК",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserResponse.class))
                    )
            )
    })
    @GetMapping(produces = "application/json")
    public List<UserResponse> list() {
        return userMapper.toResponse(userService.listAll());
    }

    @Operation(
            summary = "Счета пользователя",
            description = "Возвращает список счетов для указанного пользователя."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "ОК",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AccountResponse.class))
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping(value = "/{id}/accounts", produces = "application/json")
    public List<AccountResponse> userAccounts(
            @Parameter(description = "ID пользователя", required = true) @PathVariable Long id
    ) {
        return accountMapper.toResponse(accountService.listByUser(id));
    }
}
