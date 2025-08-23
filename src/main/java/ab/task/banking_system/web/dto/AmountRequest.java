package ab.task.banking_system.web.dto;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record AmountRequest(
        @DecimalMin(value = "0.01") BigDecimal amount
) {}
