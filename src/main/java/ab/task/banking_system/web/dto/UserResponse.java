package ab.task.banking_system.web.dto;

import java.time.Instant;

public record UserResponse(Long id, String name, String email, Instant registeredAt) {}
