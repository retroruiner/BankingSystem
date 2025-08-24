package ab.task.banking_system.web.controller;

import ab.task.banking_system.model.User;
import ab.task.banking_system.service.AccountService;
import ab.task.banking_system.service.UserService;
import ab.task.banking_system.web.dto.UserCreateRequest;
import ab.task.banking_system.web.dto.UserResponse;
import ab.task.banking_system.web.mapper.AccountMapper;
import ab.task.banking_system.web.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    UserService userService;
    @MockitoBean
    AccountService accountService;
    @MockitoBean
    UserMapper userMapper;
    @MockitoBean
    AccountMapper accountMapper;

    @Test
    void createUser_returns201() throws Exception {
        Instant now = Instant.now();

        UserCreateRequest req = new UserCreateRequest("Adilet", "adilet@example.com");

        User entity = new User();
        entity.setId(1L);
        entity.setName("Adilet");
        entity.setEmail("adilet@example.com");
        entity.setRegisteredAt(now);

        UserResponse resp = new UserResponse(1L, "Adilet", "adilet@example.com", now);

        when(userService.create("Adilet", "adilet@example.com")).thenReturn(entity);
        when(userMapper.toResponse(entity)).thenReturn(resp);

        mockMvc.perform(post("/api/users")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Adilet"))
                .andExpect(jsonPath("$.email").value("adilet@example.com"));
    }

    @Test
    void createUser_validationError_returns400() throws Exception {
        String badReqJson = """
            {"name":"Johnny"}
            """;

        mockMvc.perform(post("/api/users")
                        .contentType(APPLICATION_JSON)
                        .content(badReqJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUser_returns200() throws Exception {
        Instant now = Instant.now();

        User entity = new User();
        entity.setId(2L);
        entity.setName("Chief");
        entity.setEmail("chief@example.com");
        entity.setRegisteredAt(now);

        UserResponse resp = new UserResponse(2L, "Chief", "chief@example.com", now);

        when(userService.getById(2L)).thenReturn(entity);
        when(userMapper.toResponse(entity)).thenReturn(resp);

        mockMvc.perform(get("/api/users/2"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.email").value("chief@example.com"));
    }

    @Test
    void getUser_notFound_returns404() throws Exception {
        when(userService.getById(999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void listUsers_returns200AndArray() throws Exception {
        Instant now = Instant.now();

        User u1 = new User(); u1.setId(1L); u1.setName("A"); u1.setEmail("a@ex.com"); u1.setRegisteredAt(now);
        User u2 = new User(); u2.setId(2L); u2.setName("B"); u2.setEmail("b@ex.com"); u2.setRegisteredAt(now);

        UserResponse r1 = new UserResponse(1L, "A", "a@ex.com", now);
        UserResponse r2 = new UserResponse(2L, "B", "b@ex.com", now);

        when(userService.listAll()).thenReturn(List.of(u1, u2));
        when(userMapper.toResponse(anyList())).thenReturn(List.of(r1, r2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }
}
