package com.example.b2cdemo.controller;

import com.example.b2cdemo.config.JwtClaimsConverter;
import com.example.b2cdemo.config.SecurityConfig;
import com.example.b2cdemo.dto.request.ProductRequest;
import com.example.b2cdemo.dto.response.ProductResponse;
import com.example.b2cdemo.domain.enums.ProductCategory;
import com.example.b2cdemo.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import({SecurityConfig.class, JwtClaimsConverter.class})
class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    JwtDecoder jwtDecoder;

    @MockBean
    ProductService productService;

    // ---- GET /api/products ----

    @Test
    void listProducts_withReadScope_returns200() throws Exception {
        when(productService.findAll(any())).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/products")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_products.read"))))
                .andExpect(status().isOk());
    }

    @Test
    void listProducts_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listProducts_withoutReadScope_returns403() throws Exception {
        // Valid token, but wrong scope
        mockMvc.perform(get("/api/products")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_products.write"))))
                .andExpect(status().isForbidden());
    }

    // ---- POST /api/products ----

    @Test
    void createProduct_withWriteScope_returns201() throws Exception {
        var response = new ProductResponse(
                UUID.randomUUID(), "Widget", null,
                BigDecimal.valueOf(9.99), ProductCategory.ELECTRONICS, 10,
                Instant.now(), Instant.now());
        when(productService.create(any())).thenReturn(response);

        var request = new ProductRequest("Widget", null, BigDecimal.valueOf(9.99),
                ProductCategory.ELECTRONICS, 10);

        mockMvc.perform(post("/api/products")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_products.write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void createProduct_withOnlyReadScope_returns403() throws Exception {
        var request = new ProductRequest("Widget", null, BigDecimal.valueOf(9.99), null, null);

        mockMvc.perform(post("/api/products")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_products.read")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createProduct_withMissingRequiredFields_returns400() throws Exception {
        // name is blank — violates @NotBlank
        var request = new ProductRequest("", null, BigDecimal.valueOf(9.99), null, null);

        mockMvc.perform(post("/api/products")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_products.write")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ---- DELETE /api/products/{id} ----

    @Test
    void deleteProduct_withWriteScope_returns204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/products/" + id)
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_products.write"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_withReadOnlyScope_returns403() throws Exception {
        mockMvc.perform(delete("/api/products/" + UUID.randomUUID())
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_products.read"))))
                .andExpect(status().isForbidden());
    }
}
