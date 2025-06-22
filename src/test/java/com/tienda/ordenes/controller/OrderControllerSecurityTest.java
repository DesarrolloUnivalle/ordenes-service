package com.tienda.ordenes.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/ordenes sin token debe retornar 401")
    void listarOrdenes_sinToken_debeRetornar401() throws Exception {
        mockMvc.perform(get("/api/ordenes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/ordenes sin token debe retornar 401")
    void crearOrden_sinToken_debeRetornar401() throws Exception {
        mockMvc.perform(post("/api/ordenes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "items": [
                                    { "productoId": 1, "cantidad": 2 }
                                ]
                            }
                        """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/ordenes/confirmar-pago sin token debe retornar 401")
    void confirmarPago_sinToken_debeRetornar401() throws Exception {
        mockMvc.perform(post("/api/ordenes/confirmar-pago")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "orderId": 1,
                                "emailUsuario": "usuario@example.com"
                            }
                        """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /actuator/health sin token debe permitir acceso (200)")
    void healthCheck_sinToken_debeRetornar200() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
