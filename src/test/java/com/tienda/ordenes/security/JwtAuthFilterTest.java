package com.tienda.ordenes.security;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @Mock
    private FilterChain filterChain;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilter_tokenValido_estableceAutenticacion() throws Exception {
        String token = "valid.token.value";
        String email = "usuario@example.com";

        request.addHeader("Authorization", "Bearer " + token); // Simula que se envió un token en la solicitud

        when(jwtUtil.isTokenExpired(token)).thenReturn(false);
        when(jwtUtil.extractUsername(token)).thenReturn(email);

        jwtAuthFilter.doFilterInternal(request, response, filterChain); // Simula un token válido

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        Jwt principal = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal(); 
        assertEquals(token, principal.getTokenValue());

        verify(filterChain).doFilter(request, response); // Verifica que se llamó al siguiente filtro en la cadena
    }

    @Test
    void doFilter_tokenExpirado_retornar401() throws Exception {
        String token = "expired.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtil.isTokenExpired(token)).thenReturn(true); 

        jwtAuthFilter.doFilterInternal(request, response, filterChain); // Simula un token expirado

        assertEquals(401, response.getStatus());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain, never()).doFilter(any(), any()); // Verifica que no se llamó al siguiente filtro
    }

    @Test
    void doFilter_sinToken_noModificaContexto() throws Exception {
        jwtAuthFilter.doFilterInternal(request, response, filterChain); // No se envió token en la solicitud

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response); // Verifica que se llamó al siguiente filtro
    }

    @Test
    void doFilter_tokenConError_lanza401() throws Exception {
        String token = "error.token";
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtil.isTokenExpired(token)).thenThrow(new RuntimeException("Error simulando JWT"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain); // Simula un error al procesar el token

        assertEquals(401, response.getStatus());
        verify(filterChain, never()).doFilter(any(), any()); // Verifica que no se llamó al siguiente filtro
    }
}
