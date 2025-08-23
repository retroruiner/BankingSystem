package ab.task.banking_system.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserCreateRequest(
        @NotBlank String name,
        @Email @NotBlank String email
) {}