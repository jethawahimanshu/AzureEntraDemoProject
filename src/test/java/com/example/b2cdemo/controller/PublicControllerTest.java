package com.example.b2cdemo.controller;

import com.example.b2cdemo.config.JwtClaimsConverter;
import com.example.b2cdemo.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicController.class)
@Import({SecurityConfig.class, JwtClaimsConverter.class})
class PublicControllerTest {

    @Autowired
    MockMvc mockMvc;

    // Replaces JwtDecoderConfig bean — no real Azure B2C properties needed in tests
    @MockBean
    JwtDecoder jwtDecoder;

    @Test
    void healthEndpoint_requiresNoAuth_returns200() throws Exception {
        mockMvc.perform(get("/api/public/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void infoEndpoint_requiresNoAuth_returns200() throws Exception {
        mockMvc.perform(get("/api/public/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Azure B2C Demo API"));
    }
}
