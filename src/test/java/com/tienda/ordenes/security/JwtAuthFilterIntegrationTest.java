package com.tienda.ordenes.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthFilterIntegrationTest {

    @Autowired 
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    private final String endpointProtegido = "/api/ordenes";

    @Test
    void accederConTokenValido_deberiaRetornar200() throws Exception {
        String token = "token.valido";
        String email = "test@ejemplo.com";

        when(jwtUtil.isTokenExpired(token)).thenReturn(false);
        when(jwtUtil.extractUsername(token)).thenReturn(email);

        mockMvc.perform(get("/test/protegido")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()); // Verifica que el acceso sea permitido con un token válido
    }

    @Test
    void accederConTokenExpirado_deberiaRetornar401() throws Exception {
        String token = "expired.token.value";

        when(jwtUtil.isTokenExpired(token)).thenReturn(true);

        mockMvc.perform(get(endpointProtegido)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()); // Debería retornar 401 Unauthorized, no 403 Forbidden
    }

    @Test
    void accederSinToken_deberiaRetornar403() throws Exception {
        mockMvc.perform(get(endpointProtegido)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()); // Debería retornar 401 Unauthorized, no 403 Forbidden
    }

    @Test
    void tokenValidoPeroSinUsername_deberiaRetornar401() throws Exception {
    String token = "token.sin.username";

    when(jwtUtil.isTokenExpired(token)).thenReturn(false);
    when(jwtUtil.extractUsername(token)).thenReturn(null);

    mockMvc.perform(get(endpointProtegido)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized()); // Debería retornar 401 Unauthorized, no 403 Forbidden
    }

    @Test
    void tokenInvalido_lanzaExcepcion_deberiaRetornar401() throws Exception {
    String token = "token.invalido";

    when(jwtUtil.isTokenExpired(token)).thenThrow(new RuntimeException("token inválido"));

    mockMvc.perform(get(endpointProtegido)
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized()); // Debería retornar 401 Unauthorized, no 403 Forbidden
    }

    @Test
    void authorizationHeaderMalformado_deberiaRetornar403() throws Exception {
    mockMvc.perform(get(endpointProtegido)
            .header("Authorization", "TokenInvalidoSinBearer")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());  // Debería retornar 401 Unauthorized, no 403 Forbidden
    }

    @Test
    void tokenValidoSinRoles_deberiaPermitirAcceso() throws Exception {
    String token = "token.sin.roles";
    String email = "usuario@ejemplo.com";

    when(jwtUtil.isTokenExpired(token)).thenReturn(false);
    when(jwtUtil.extractUsername(token)).thenReturn(email);

    mockMvc.perform(get("/test/protegido")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()); // Verifica que el acceso sea permitido incluso sin roles
    }

}
