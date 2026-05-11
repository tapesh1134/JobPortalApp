package org.jobportal.authservice;

import org.jobportal.authservice.entity.Role;
import org.jobportal.authservice.entity.UserCredential;
import org.jobportal.authservice.repository.AuthRepository;
import org.jobportal.authservice.security.jwt.JwtUtil;
import org.jobportal.authservice.service.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthServiceApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AuthRepository authRepository;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void testRegisterUser_success() throws Exception {

        UserCredential user = new UserCredential();
        user.setEmail("test@gmail.com");
        user.setRole(Role.CANDIDATE);

        Mockito.when(authService.register(any())).thenReturn(user);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email":"test@gmail.com",
                                    "password":"123456",
                                    "role":"CANDIDATE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User Created"));
    }

    @Test
    void testLogin_success_setsCookie() throws Exception {

        Mockito.when(authService.login(any())).thenReturn("mock-jwt-token");
        Mockito.when(authService.getByEmail(anyString())).thenReturn(new UserCredential());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "email":"test@gmail.com",
                                    "password":"123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "test@gmail.com")
    void testValidateToken_success() throws Exception {

        UserCredential user = new UserCredential();
        user.setEmail("test@gmail.com");

        Mockito.when(authService.getByEmail(anyString())).thenReturn(user);

        mockMvc.perform(post("/auth/validate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "test@gmail.com")
    void testUpdateRole_success() throws Exception {

        UserCredential user = new UserCredential();
        user.setEmail("test@gmail.com");
        user.setRole(Role.CANDIDATE);

        Mockito.when(authService.getByEmail(anyString())).thenReturn(user);
        Mockito.when(jwtUtil.generateToken(any())).thenReturn("new-token");

        mockMvc.perform(put("/auth/role")
                        .param("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Set-Cookie"))
                .andExpect(jsonPath("$.success").value(true));
    }
}
