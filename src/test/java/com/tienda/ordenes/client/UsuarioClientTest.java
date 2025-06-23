package com.tienda.ordenes.client;

import com.tienda.ordenes.dto.UserResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UsuarioClientTest {

    private UsuarioClient usuarioClient;

    @BeforeEach
    void setup() {
        usuarioClient = new UsuarioClient() {
            @Override
            public UserResponseDTO obtenerUsuarioInternal(Long id) {
                UserResponseDTO dto = new UserResponseDTO();
                dto.setUsuarioId(id);
                dto.setCorreo("correo@ejemplo.com");
                return dto;
            }

            @Override
            public UserResponseDTO obtenerUsuarioPorEmailInternal(String email) {
                UserResponseDTO dto = new UserResponseDTO();
                dto.setCorreo(email);
                dto.setUsuarioId(1L);
                return dto;
            }
        };
    }

    @Test
    void obtenerUsuario_deberiaRetornarUsuario() {
        // Simula un ID de usuario de prueba
        UserResponseDTO usuario = usuarioClient.obtenerUsuario(10L);
        assertNotNull(usuario);
        assertEquals(10L, usuario.getUsuarioId());
    }

    @Test
    void obtenerUsuarioPorEmail_deberiaRetornarUsuario() {
        // Simula un email de prueba
        String email = "prueba@correo.com";
        UserResponseDTO usuario = usuarioClient.obtenerUsuarioPorEmail(email);
        assertNotNull(usuario);
        assertEquals(email, usuario.getCorreo());
    }
}
