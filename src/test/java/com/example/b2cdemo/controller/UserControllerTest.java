package com.example.b2cdemo.controller;

import com.example.b2cdemo.config.JwtClaimsConverter;
import com.example.b2cdemo.config.SecurityConfig;
import com.example.b2cdemo.dto.request.UserProfileRequest;
import com.example.b2cdemo.dto.response.UserProfileResponse;
import com.example.b2cdemo.service.UserService;
import com.example.b2cdemo.support.JwtTestFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, JwtClaimsConverter.class})
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    JwtDecoder jwtDecoder;

    @MockBean
    UserService userService;

    @Test
    void getProfile_withValidToken_returns200() throws Exception {
        var response = new UserProfileResponse(
                JwtTestFactory.TEST_OID,
                JwtTestFactory.TEST_NAME,
                JwtTestFactory.TEST_EMAIL,
                null,
                Instant.now(),
                Instant.now()
        );
        when(userService.getProfile(eq(JwtTestFactory.TEST_OID), any())).thenReturn(response);

        mockMvc.perform(get("/api/user/profile")
                        .with(jwt().jwt(JwtTestFactory.userJwt())
                                   .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.oid").value(JwtTestFactory.TEST_OID));
    }

    @Test
    void getProfile_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/user/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateProfile_withValidToken_returns200() throws Exception {
        var request = new UserProfileRequest("New Name", null);
        var response = new UserProfileResponse(
                JwtTestFactory.TEST_OID, "New Name", JwtTestFactory.TEST_EMAIL,
                null, Instant.now(), Instant.now()
        );
        when(userService.upsertProfile(eq(JwtTestFactory.TEST_OID), any(), any())).thenReturn(response);

        mockMvc.perform(put("/api/user/profile")
                        .with(jwt().jwt(JwtTestFactory.userJwt()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("New Name"));
    }

    @Test
    void updateProfile_withInvalidPhoneNumber_returns400() throws Exception {
        var request = new UserProfileRequest("Name", "not-a-phone-number!!!");

        mockMvc.perform(put("/api/user/profile")
                        .with(jwt().jwt(JwtTestFactory.userJwt()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
