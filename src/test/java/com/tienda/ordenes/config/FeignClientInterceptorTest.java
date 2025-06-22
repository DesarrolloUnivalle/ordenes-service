package com.tienda.ordenes.config;

import feign.RequestTemplate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeignClientInterceptorTest {

    private final FeignClientInterceptor interceptor = new FeignClientInterceptor();

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void interceptor_agregaAuthorizationHeader_siJwtPresente() {
        // Arrange
        String token = "fake.jwt.token";
        Jwt jwt = Jwt.withTokenValue(token)
                .header("alg", "none")
                .claim("sub", "usuario@ejemplo.com")
                .build();

        var auth = new UsernamePasswordAuthenticationToken(jwt, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        RequestTemplate template = new RequestTemplate();

        // Act
        interceptor.apply(template);

        // Assert
        assertTrue(template.headers().containsKey("Authorization"));
        assertEquals("Bearer " + token, template.headers().get("Authorization").iterator().next());
    }

    @Test
    void interceptor_noAgregaHeader_siPrincipalNoEsJwt() {
        // Arrange
        var auth = new UsernamePasswordAuthenticationToken("otroUsuario", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        RequestTemplate template = new RequestTemplate();

        // Act
        interceptor.apply(template);

        // Assert
        assertTrue(template.headers().isEmpty(), "No debería haber headers agregados");
    }

    @Test
    void interceptor_noFalla_siNoHayAutenticacion() {
        // Arrange
        SecurityContextHolder.clearContext();
        RequestTemplate template = new RequestTemplate();

        // Act
        interceptor.apply(template);

        // Assert
        assertTrue(template.headers().isEmpty(), "No debería haber headers si no hay autenticación");
    }
}
