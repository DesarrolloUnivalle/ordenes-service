package com.tienda.ordenes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import java.util.List;

public class OrderRequest {
    @NotEmpty(message = "La lista de items no puede estar vacía")
    @Valid
    private List<OrderItemRequest> items;

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }

    public static class OrderItemRequest {
        @NotNull(message = "El ID del producto es requerido")
        private Long productoId;

        @NotNull(message = "La cantidad es requerida")
        @Min(1) // Asegura que la cantidad sea al menos 1   
        private Integer cantidad;

        @NotNull(message = "El precio es requerido")
        @Positive(message = "El precio debe ser positivo")
        private Double precio;

        public Long getProductoId() {
            return productoId;
        }

        public void setProductoId(Long productoId) {
            this.productoId = productoId;
        }

        public Integer getCantidad() {
            return cantidad;
        }

        public void setCantidad(Integer cantidad) {
            this.cantidad = cantidad;
        }

        public Double getPrecio() {
            return precio;
        }

        public void setPrecio(Double precio) {
            this.precio = precio;
        }
    }
}