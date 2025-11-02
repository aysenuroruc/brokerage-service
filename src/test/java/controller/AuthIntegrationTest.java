package controller;
import com.brokerage.service.BrokerageServiceApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.Role;
import model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import repository.UserRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@SpringBootTest(classes = BrokerageServiceApplication.class)
public class AuthIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanDatabase() {
        userRepository.deleteAll();
    }

    @Test
    void login_returns_jwt_token() throws Exception {
        // Arrange
        User user = new User(null, "loginuser", passwordEncoder.encode("secret"), Role.CUSTOMER, 42L);
        userRepository.save(user);

        var requestBody = objectMapper.writeValueAsString(new java.util.HashMap<String, String>() {{
            put("username", "loginuser");
            put("password", "secret");
        }});

        // Act
        var mvcResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        // Assert
        var response = mvcResult.getResponse().getContentAsString();
        var json = objectMapper.readTree(response);

        assertThat(json.has("token")).isTrue();
        assertThat(json.get("token").asText()).isNotBlank();
    }
}
