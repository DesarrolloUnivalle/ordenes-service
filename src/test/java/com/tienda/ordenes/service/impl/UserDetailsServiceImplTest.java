package com.tienda.ordenes.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;


import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;

class UserDetailsServiceImplTest {

    private UserDetailsServiceImpl service;

    private final UserDetailsServiceImpl userDetailsService = new UserDetailsServiceImpl();

    @BeforeEach
    void setUp() {
    service = new UserDetailsServiceImpl();
    }

    @Test
    void loadUserByUsername_deberiaCrearUserDetailsConEmail() {
        String email = "usuario@correo.com";

        UserDetails userDetails = userDetailsService.loadUserByUsername(email); // Simula la carga de usuario por email

        assertEquals(email, userDetails.getUsername());
        assertEquals("", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER"))); // Verifica que tenga el rol de usuario
    }

    @Test
    void loadUserByUsername_emailNulo_lanzaExcepcion() {
    assertThrows(IllegalArgumentException.class, () -> { 
        service.loadUserByUsername(null); // Simula la carga de usuario con email nulo
    });
    }

}
