package com.tienda.ordenes.security;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestSecureController {

    @GetMapping("/test/protegido")
    public String protegido() {
        return "Acceso concedido";
    }
}
