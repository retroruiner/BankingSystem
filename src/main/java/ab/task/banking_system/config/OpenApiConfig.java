package ab.task.banking_system.config;

import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Banking System API",
                version = "v1",
                description = "REST API для пользователей и счетов",
                contact = @Contact(name = "Banking Team", email = "support@example.com")
        ),
        servers = { @Server(url = "/", description = "Default") }
)
public class OpenApiConfig { }
