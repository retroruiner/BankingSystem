package ab.task.banking_system.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AccountCreateRequest(
        @NotNull Long userId,
        @Size(min = 6, max = 34) String number
) {}
