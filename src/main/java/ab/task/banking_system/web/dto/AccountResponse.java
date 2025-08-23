package ab.task.banking_system.web.dto;

import java.math.BigDecimal;

public record AccountResponse(Long id, String number, BigDecimal balance, Long userId) {}
