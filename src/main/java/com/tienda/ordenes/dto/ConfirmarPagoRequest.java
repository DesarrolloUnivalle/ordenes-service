package com.tienda.ordenes.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.experimental.NonFinal;

public class ConfirmarPagoRequest {
    private Long orderId;
    private String emailUsuario;

    // Getters y setters
    @NotNull(message = "El ID de la orden es requerido")
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    @NotNull(message = "El email del usuario es requerido")
    @Email(message = "El email debe ser válido")    
    public String getEmailUsuario() {
        return emailUsuario;
    }

    public void setEmailUsuario(String emailUsuario) {
        this.emailUsuario = emailUsuario;
    }
}
