package ab.task.banking_system.web.mapper;

import ab.task.banking_system.model.Account;
import ab.task.banking_system.web.dto.AccountResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    @Mapping(source = "user.id", target = "userId")
    AccountResponse toResponse(Account account);
}
