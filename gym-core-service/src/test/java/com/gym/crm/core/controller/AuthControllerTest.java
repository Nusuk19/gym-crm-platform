package com.gym.crm.core.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gia.openapi.model.LoginChangeRequest;
import com.gia.openapi.model.LoginRequest;
import com.gia.openapi.model.LoginResponse;
import com.gym.crm.core.facade.GymFacade;
import com.gym.crm.core.security.GymUserDetailsService;
import com.gym.crm.core.security.JwtService;
import com.gym.crm.core.security.TokenBlacklistService;
import com.gym.crm.core.util.JsonResourceReader;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    private static final String USERNAME = "Abdul.Hariton";
    private static final String PASSWORD = "password123";
    private static final String NEW_PASSWORD = "newPassword123";
    private static final String TOKEN = "jwt-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GymFacade facade;

    @MockBean
    private GymUserDetailsService gymUserDetailsService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void login_validRequest_returns200WithToken() throws Exception {
        String request = JsonResourceReader.readResource("/json/auth/auth-login-request.json");
        String expectedResponse = JsonResourceReader.readResource("/json/auth/auth-login-response.json");
        LoginResponse response = new LoginResponse()
                .username(USERNAME)
                .token(TOKEN);

        when(facade.login(any(LoginRequest.class))).thenReturn(response);

        String actualResponse = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JSONAssert.assertEquals(expectedResponse, actualResponse, true);
        verify(facade).login(any(LoginRequest.class));
    }

    @Test
    void login_nullUsername_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(null, PASSWORD))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(facade);
    }

    @Test
    void login_nullPassword_returns400() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(USERNAME, null))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(facade);
    }

    @Test
    void changePassword_validRequest_returns200() throws Exception {
        String request = JsonResourceReader.readResource("/json/auth/auth-change-password-request.json");

        mockMvc.perform(put("/api/v1/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());

        verify(facade).changePassword(any(LoginChangeRequest.class));
    }

    @Test
    void changePassword_nullUsername_returns400() throws Exception {
        mockMvc.perform(put("/api/v1/auth/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginChangeRequest(null, PASSWORD, NEW_PASSWORD))))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(facade);
    }

    @Test
    void logout_shouldReturnOk() throws Exception {
        String authorizationHeader = "Bearer jwt-token";

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header(HttpHeaders.AUTHORIZATION, authorizationHeader))
                .andExpect(status().isOk());

        verify(facade).logout(authorizationHeader);
    }
}