package com.tienda.ordenes.repository;

import com.tienda.ordenes.model.Order;
import com.tienda.ordenes.model.OrderItem;
import com.tienda.ordenes.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void guardarYBuscarOrdenPorUsuarioId_deberiaRetornarOrden() {
        // Preparar orden
        Order order = new Order();
        order.setUsuarioId(10L);
        order.setFechaCreacion(LocalDateTime.now());
        order.setEstado(OrderStatus.PENDIENTE);
        order.setTotal(100.0);

        OrderItem item = new OrderItem();
        item.setProductoId(1L);
        item.setCantidad(2);
        item.setPrecio(50.0);
        item.setOrden(order);

        order.setDetalles(List.of(item));

        // Guardar
        Order saved = orderRepository.save(order);
        assertNotNull(saved.getId());

        // Buscar por usuarioId
        List<Order> ordenes = orderRepository.findByUsuarioId(10L);
        assertEquals(1, ordenes.size());
        assertEquals(saved.getId(), ordenes.get(0).getId());
        assertEquals(10L, ordenes.get(0).getUsuarioId());
    }

    @Test
    void buscarPorUsuarioIdInexistente_deberiaRetornarListaVacia() {
        List<Order> ordenes = orderRepository.findByUsuarioId(999L);
        assertTrue(ordenes.isEmpty()); // Verifica que no se encuentren órdenes para un usuario inexistente
    }
}
