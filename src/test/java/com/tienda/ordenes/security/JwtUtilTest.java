package com.tienda.ordenes.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String secret = "bXlTdXBlclNlY3JldEtleU15U3VwZXJTZWNyZXRLZXk="; // base64 de 32 bytes
    private long expiration = 1000 * 60 * 60; // 1 hora
    private String issuer = "mi-emisor";

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", expiration);
        ReflectionTestUtils.setField(jwtUtil, "issuer", issuer);
    }

    @Test
    void generateToken_y_extractUsername_deberianCoincidir() {
        String email = "usuario@example.com";
        String token = jwtUtil.generateToken(email);

        String usernameExtraido = jwtUtil.extractUsername(token);

        assertEquals(email, usernameExtraido); // Verifica que el nombre de usuario extraído del token coincida con el email utilizado para generarlo
    }

    @Test
    void isTokenExpired_deberiaDetectarTokenExpirado() {
        String expiredToken = Jwts.builder()
                .setSubject("usuario@example.com")
                .setExpiration(new Date(System.currentTimeMillis() - 1000))
                .signWith(ReflectionTestUtils.invokeMethod(jwtUtil, "getSigningKey"), SignatureAlgorithm.HS256)
                .compact();

        assertTrue(jwtUtil.isTokenExpired(expiredToken)); // El token expirado debería ser detectado como tal
    }

    @Test
    void isTokenExpired_deberiaDetectarTokenValido() {
        String token = jwtUtil.generateToken("usuario@example.com");
        assertFalse(jwtUtil.isTokenExpired(token)); // El token recién generado no debería estar expirado
    }

    @Test
    void extractClaim_deberiaExtraerCorrectamente() {
        String token = jwtUtil.generateToken("usuario@example.com");
        Function<Claims, String> claimFn = Claims::getSubject;
        String result = jwtUtil.extractClaim(token, claimFn);

        assertEquals("usuario@example.com", result); // Verifica que el claim extraído sea el correcto
    } 

    @Test
    void isTokenValid_deberiaRetornarTrueParaTokenValido() {
        String token = jwtUtil.generateToken("usuario@example.com");
        boolean valido = jwtUtil.isTokenValid(token, "usuario@example.com");
        assertTrue(valido); // El token es válido para el usuario correcto
    }

    @Test
    void isTokenValid_conUsernameIncorrecto_deberiaRetornarFalse() {
        String token = jwtUtil.generateToken("otro@correo.com");
        boolean valido = jwtUtil.isTokenValid(token, "usuario@example.com");
        assertFalse(valido); // El token no es válido para un usuario diferente
    }

    @Test
    void extractUsername_conTokenMalformado_deberiaRetornarNull() {
        String tokenMalformado = "token.invalido";
        String username = jwtUtil.extractUsername(tokenMalformado);
        assertNull(username); // Un token malformado no puede extraer el nombre de usuario
    }

    @Test
    void extractExpiration_conTokenMalformado_deberiaRetornarNull() {
        String tokenMalformado = "token.invalido";
        Date expiration = jwtUtil.extractExpiration(tokenMalformado);
        assertNull(expiration); // Un token malformado no puede extraer la fecha de expiración
    }

    @Test
    void extractClaim_conTokenMalformado_deberiaRetornarNull() {
        String tokenMalformado = "token.invalido";
        String claim = jwtUtil.extractClaim(tokenMalformado, Claims::getSubject);
        assertNull(claim); // Un token malformado no puede extraer claims
    }

    @Test
    void isTokenExpired_conTokenMalformado_deberiaRetornarTrue() {
        String tokenMalformado = "token.invalido";
        assertTrue(jwtUtil.isTokenExpired(tokenMalformado)); // Un token malformado se considera expirado
    }

}
