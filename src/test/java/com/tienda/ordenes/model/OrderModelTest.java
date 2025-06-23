package com.tienda.ordenes.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tienda.ordenes.dto.OrderItemDTO;
import com.tienda.ordenes.dto.OrderRequest;
import com.tienda.ordenes.dto.UserResponseDTO;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


import static org.junit.jupiter.api.Assertions.*;


public class OrderModelTest {

    private Validator validator;

    @BeforeEach
    void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testOrderEntity() {
        Order order = new Order();
        order.setId(10L);
        order.setUsuarioId(5L);
        order.setFechaCreacion(LocalDateTime.of(2024, 1, 1, 12, 0));
        order.setEstado(OrderStatus.PENDIENTE);
        order.setTotal(100.50);

        // Crea un OrderItem y lo asocia a la orden
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setCantidad(2);
        item.setPrecio(50.25);
        item.setProductoId(101L);
        item.setOrden(order);

        order.setDetalles(List.of(item));

        // Verifica que los campos se establezcan correctamente
        assertEquals(10L, order.getId());
        assertEquals(5L, order.getUsuarioId());
        assertEquals(OrderStatus.PENDIENTE, order.getEstado());
        assertEquals(100.50, order.getTotal());
        assertNotNull(order.getDetalles());
        assertEquals(1, order.getDetalles().size());
        assertEquals(order, item.getOrden());
    }

    @Test
    void testOrderItemEntity() {
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setCantidad(3);
        item.setPrecio(20.0);
        item.setProductoId(202L);

        // Verifica que los campos se establezcan correctamente
        assertEquals(1L, item.getId());
        assertEquals(3, item.getCantidad());
        assertEquals(20.0, item.getPrecio());
        assertEquals(202L, item.getProductoId());
    }

    @Test
    void testOrderStatusEnum() {
        // Verifica que los valores del enum OrderStatus sean correctos
        assertEquals("Pendiente", OrderStatus.PENDIENTE.getValue());
        assertEquals("Pagada", OrderStatus.PAGADA.getValue());
        assertEquals("Enviada", OrderStatus.ENVIADA.getValue());
        assertEquals("Entregada", OrderStatus.ENTREGADA.getValue());
        assertEquals("Cancelada", OrderStatus.CANCELADA.getValue());
    }

    @Test
    void testOrderStatusConverter() {
        OrderStatusConverter converter = new OrderStatusConverter();

        assertEquals("Pagada", converter.convertToDatabaseColumn(OrderStatus.PAGADA));
        assertEquals(OrderStatus.ENTREGADA, converter.convertToEntityAttribute("Entregada"));

        // Verifica que el convertidor maneje correctamente valores nulos y desconocidos
        assertNull(converter.convertToDatabaseColumn(null));
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("Desconocido"));
    }

    @Test
    void testOrderItemDTO_CantidadNegativa() {
        // Crea una instancia de OrderItemDTO con cantidad negativa
        OrderItemDTO dto = new OrderItemDTO();
        dto.setProductoId(2L);
        dto.setCantidad(-5);

        Set<ConstraintViolation<OrderItemDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty()); // Verifica que haya violaciones de validación
    }

    @Test
    void testOrderRequest_conItemInvalido_deberiaFallar() {
        OrderRequest.OrderItemRequest item = new OrderRequest.OrderItemRequest();
        item.setCantidad(null); // cantidad requerida
        item.setPrecio(100.0); // válido

        OrderRequest request = new OrderRequest();
        request.setItems(List.of(item)); // contiene item inválido

        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty()); 
    }

    @Test
    void testOrderRequest_ItemsNull_deberiaFallar() {
        OrderRequest request = new OrderRequest(); // items es null

        Set<ConstraintViolation<OrderRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty()); // Verifica que haya violaciones de validación
    }

    @Test
    void testUserResponseDTO_toString() {
        // Crea una instancia de UserResponseDTO
        UserResponseDTO user = new UserResponseDTO(); 
        user.setUsuarioId(1L);
        user.setNombre("Test");
        user.setCorreo("test@correo.com");
        user.setRol("ADMIN");

        // Verifica que el método toString incluya los campos esperados
        String str = user.toString();
        assertTrue(str.contains("UserResponseDTO"));
        assertTrue(str.contains("Test")); // Verifica que el nombre esté presente
    }

}
