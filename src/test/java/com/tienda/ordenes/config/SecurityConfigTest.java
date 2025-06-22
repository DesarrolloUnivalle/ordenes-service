package com.tienda.ordenes.config;

import com.tienda.ordenes.security.JwtAuthFilter;
import com.tienda.ordenes.security.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfigTest.TestControllerConfig.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("Acceder a ruta pública sin autenticación debe retornar 200")
    void accederARutaPublica_sinToken_deberiaPermitir() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Acceder a ruta protegida sin token debe retornar 401")
    void accederARutaProtegida_sinToken_deberiaDenegar() throws Exception {
        mockMvc.perform(get("/protegido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Acceder a ruta protegida con token debe retornar 200")
    void accederARutaProtegida_conTokenValido_deberiaPermitir() throws Exception {
        String token = "token-valido";
        String username = "usuario@example.com";

        // Simulación de comportamiento del JwtUtil
        given(jwtUtil.isTokenExpired(token)).willReturn(false);
        given(jwtUtil.extractUsername(token)).willReturn(username);

        Jwt jwt = Jwt.withTokenValue(token)
                .header("alg", "HS256")
                .claim("sub", username)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        mockMvc.perform(get("/protegido")
                        .header("Authorization", "Bearer " + token)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(jwt, null, List.of())
                        )))
                .andExpect(status().isOk());
    }

    @TestConfiguration
    static class TestControllerConfig {
        @RestController
        static class Controller {

            @GetMapping("/actuator/health")
            public String health() {
                return "OK";
            }

            @GetMapping("/protegido")
            public String protegido() {
                return "Acceso permitido";
            }
        }

        @Bean
        public JwtAuthFilter jwtAuthFilter(JwtUtil jwtUtil) {
            return new JwtAuthFilter(jwtUtil);
        }
    }
}
