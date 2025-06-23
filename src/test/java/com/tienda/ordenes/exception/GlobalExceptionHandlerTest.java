package com.tienda.ordenes.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleAllExceptions_deberiaRetornar500() {
        Exception exception = new RuntimeException("Error inesperado");
        WebRequest request = mock(WebRequest.class);
        when(request.getDescription(false)).thenReturn("/api/test");

        ResponseEntity<Object> response = handler.handleAllExceptions(exception, request);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(body);
        assertEquals(500, body.get("status"));
        assertEquals("Error interno del servidor", body.get("error"));
        assertEquals("Error inesperado", body.get("message"));
        assertEquals("/api/test", body.get("path"));
    }

    @Test
    void testHandleStockInsuficienteException_deberiaRetornar400() {
        StockInsuficienteException ex = new StockInsuficienteException("Stock insuficiente para el producto");
        WebRequest request = mock(WebRequest.class);
        when(request.getDescription(false)).thenReturn("/api/productos/validar-stock");

        ResponseEntity<Object> response = handler.handleStockInsuficienteException(ex, request);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(body);
        assertEquals(400, body.get("status"));
        assertEquals("Stock insuficiente", body.get("error"));
        assertEquals("Stock insuficiente para el producto", body.get("message"));
        assertEquals("/api/productos/validar-stock", body.get("path"));
    }
}
