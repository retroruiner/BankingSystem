package ab.task.banking_system.web.mapper;

import ab.task.banking_system.model.User;
import ab.task.banking_system.web.dto.UserCreateRequest;
import ab.task.banking_system.web.dto.UserResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toEntity(UserCreateRequest req);
    UserResponse toResponse(User user);
    List<UserResponse> toResponse(List<User> users);
}
