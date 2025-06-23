package com.tienda.ordenes.dto;

import org.junit.jupiter.api.Test;
import jakarta.validation.*;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DTOTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void testConfirmarPagoRequest_SetAndGet() {
        ConfirmarPagoRequest req = new ConfirmarPagoRequest();
        req.setOrderId(123L);
        req.setEmailUsuario("test@correo.com");

        assertEquals(123L, req.getOrderId());
        assertEquals("test@correo.com", req.getEmailUsuario());
    }

    @Test
    void testOrderItemDTO_Valid() {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setProductoId(1L);
        dto.setCantidad(2);

        Set<ConstraintViolation<OrderItemDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testOrderItemDTO_Invalid() {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setCantidad(0);  // productoId es null y cantidad < 1

        Set<ConstraintViolation<OrderItemDTO>> violations = validator.validate(dto);
        assertEquals(2, violations.size());
    }

    @Test
    void testOrderItemResponseDTO_Builder() {
        OrderItemResponseDTO item = OrderItemResponseDTO.builder()
                .productoId(5L)
                .cantidad(4)
                .precio(100.0)
                .build();

        assertEquals(5L, item.getProductoId());
        assertEquals(4, item.getCantidad());
        assertEquals(100.0, item.getPrecio());
    }

    @Test
    void testOrderRequest_Valid() {
        OrderRequest.OrderItemRequest item = new OrderRequest.OrderItemRequest();
        item.setProductoId(1L);
        item.setCantidad(1);
        item.setPrecio(10.0);

        OrderRequest request = new OrderRequest();
        request.setItems(List.of(item));

        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testOrderRequest_Invalid() {
        OrderRequest request = new OrderRequest();
        request.setItems(List.of());

        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testUserResponseDTO() {
        UserResponseDTO user = new UserResponseDTO();
        user.setUsuarioId(10L);
        user.setNombre("Ana");
        user.setCorreo("ana@correo.com");
        user.setRol("CLIENTE");

        assertEquals(10L, user.getUsuarioId());
        assertEquals("Ana", user.getNombre());
        assertEquals("ana@correo.com", user.getCorreo());
        assertEquals("CLIENTE", user.getRol());
    }

    @Test
    void testOrderItemRequest_InvalidValues() {
        OrderRequest.OrderItemRequest item = new OrderRequest.OrderItemRequest();
        item.setProductoId(null);  // inválido
        item.setCantidad(0);       // inválido
        item.setPrecio(-5.0);      // inválido

        Set<ConstraintViolation<OrderRequest.OrderItemRequest>> violations = validator.validate(item);
        assertEquals(3, violations.size()); // Se esperan 3 violaciones
    }

    @Test
    void testOrderItemRequest_Valid() {
        OrderRequest.OrderItemRequest item = new OrderRequest.OrderItemRequest();
        item.setProductoId(1L);
        item.setCantidad(2);
        item.setPrecio(100.0);

        Set<ConstraintViolation<OrderRequest.OrderItemRequest>> violations = validator.validate(item);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testOrderRequest_NullItemsList() {
        OrderRequest request = new OrderRequest();
        request.setItems(null); // La lista es nula

        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void testConfirmarPagoRequest_InvalidValues() {
        ConfirmarPagoRequest req = new ConfirmarPagoRequest();
        req.setOrderId(null);
        req.setEmailUsuario(null);

        Set<ConstraintViolation<ConfirmarPagoRequest>> violations = validator.validate(req);
        assertEquals(2, violations.size()); // Esperamos dos errores: email y orderId nulos
    }

}
