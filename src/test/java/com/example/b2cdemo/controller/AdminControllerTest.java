package com.example.b2cdemo.controller;

import com.example.b2cdemo.config.JwtClaimsConverter;
import com.example.b2cdemo.config.SecurityConfig;
import com.example.b2cdemo.dto.response.UserProfileResponse;
import com.example.b2cdemo.service.UserService;
import com.example.b2cdemo.support.JwtTestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@Import({SecurityConfig.class, JwtClaimsConverter.class})
class AdminControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    JwtDecoder jwtDecoder;

    @MockBean
    UserService userService;

    @Test
    void listUsers_withAdminRole_returns200() throws Exception {
        when(userService.findAllUsers(any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/admin/users")
                        .with(jwt().jwt(JwtTestFactory.adminJwt())
                                   .authorities(new SimpleGrantedAuthority("ROLE_Admin"))))
                .andExpect(status().isOk());
    }

    @Test
    void listUsers_withoutAdminRole_returns403() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .with(jwt().jwt(JwtTestFactory.userJwt())))
                .andExpect(status().isForbidden());
    }

    @Test
    void listUsers_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUser_withAdminRole_returns200() throws Exception {
        var response = new UserProfileResponse(
                JwtTestFactory.TEST_OID, JwtTestFactory.TEST_NAME,
                JwtTestFactory.TEST_EMAIL, null, null, null);
        when(userService.getProfileByOid(JwtTestFactory.TEST_OID)).thenReturn(response);

        mockMvc.perform(get("/api/admin/users/" + JwtTestFactory.TEST_OID)
                        .with(jwt().jwt(JwtTestFactory.adminJwt())
                                   .authorities(new SimpleGrantedAuthority("ROLE_Admin"))))
                .andExpect(status().isOk());
    }
}
