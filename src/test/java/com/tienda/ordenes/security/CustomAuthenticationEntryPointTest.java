package com.tienda.ordenes.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

import static org.mockito.Mockito.*;

class CustomAuthenticationEntryPointTest {

    @Test
    void commence_deberiaRetornar401Unauthorized() throws IOException {
        // Arrange
        CustomAuthenticationEntryPoint entryPoint = new CustomAuthenticationEntryPoint();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        AuthenticationException exception = mock(AuthenticationException.class);

        // Act
        entryPoint.commence(request, response, exception);

        // Assert
        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"); // Verifica que se envió el error 401 Unauthorized
    }
}
