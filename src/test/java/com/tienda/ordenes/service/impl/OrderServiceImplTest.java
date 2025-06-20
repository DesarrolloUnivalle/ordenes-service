package com.tienda.ordenes.service.impl;

import com.tienda.ordenes.client.ProductoClient;
import com.tienda.ordenes.client.UsuarioClient;
import com.tienda.ordenes.dto.OrderRequest;
import com.tienda.ordenes.dto.OrderRequest.OrderItemRequest;
import com.tienda.ordenes.dto.OrderResponse;
import com.tienda.ordenes.dto.UserResponseDTO;
import com.tienda.ordenes.model.Order;
import com.tienda.ordenes.model.OrderStatus;
import com.tienda.ordenes.repository.OrderRepository;
import com.tienda.ordenes.service.EmailService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class OrderServiceImplTest {

    @InjectMocks
    private OrderServiceImpl orderServiceImpl;

    @Mock
    private UsuarioClient usuarioClient;

    @Mock
    private ProductoClient productoClient;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCrearOrden_CuandoDatosValidos_DeberiaCrearYRetornarOrden() {
        String userEmail = "usuario@example.com";

        // Simula el usuario que responde el microservicio de usuarios
        UserResponseDTO usuario = new UserResponseDTO();
        usuario.setUsuarioId(1L);
        usuario.setCorreo(userEmail);
        usuario.setNombre("Juan");

        // Construcción de los items de la orden
        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductoId(101L);
        itemRequest.setCantidad(2);
        itemRequest.setPrecio(15.0);

        OrderRequest request = new OrderRequest();
        request.setItems(List.of(itemRequest));

        when(usuarioClient.obtenerUsuarioPorEmail(userEmail)).thenReturn(usuario);

        // Simula el guardado de la orden
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order ordenGuardada = invocation.getArgument(0);
            ordenGuardada.setId(123L);
            ordenGuardada.setFechaCreacion(LocalDateTime.now());
            ordenGuardada.setEstado(OrderStatus.PENDIENTE);
            return ordenGuardada;
        });

        // Ejecutar
        OrderResponse response = orderServiceImpl.crearOrden(userEmail, request);

        // Verificar
        assertNotNull(response);
        assertEquals(123L, response.getId());
        assertEquals(OrderStatus.PENDIENTE, response.getStatus());
        assertEquals(1, response.getItems().size());
        assertEquals(30.0, response.getTotal()); // 2 x 15.0
    }

    @Test
    void testCrearOrden_CuandoUsuarioNoExiste_DeberiaLanzarExcepcion() {
        String userEmail = "usuario_invalido@example.com";

        // Simulamos que el usuario no existe (usuarioId es null)
        UserResponseDTO usuarioInvalido = new UserResponseDTO();
        usuarioInvalido.setUsuarioId(null);

        when(usuarioClient.obtenerUsuarioPorEmail(userEmail)).thenReturn(usuarioInvalido);

        OrderItemRequest itemRequest = new OrderItemRequest();
        itemRequest.setProductoId(101L);
        itemRequest.setCantidad(1);
        itemRequest.setPrecio(10.0);

        OrderRequest request = new OrderRequest();
        request.setItems(List.of(itemRequest));

        // Ejecutar y verificar que lanza excepción
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> orderServiceImpl.crearOrden(userEmail, request)
        );

        assertEquals("El usuario no existe o no tiene un ID válido.", ex.getMessage());
    }

    @Test
    void testCrearOrden_CuandoStockInvalido_DeberiaLanzarExcepcion() {
        String userEmail = "cliente@example.com";

        // Simular usuario válido
        UserResponseDTO usuario = new UserResponseDTO();
        usuario.setUsuarioId(1L);
        when(usuarioClient.obtenerUsuarioPorEmail(userEmail)).thenReturn(usuario);

        // Preparar solicitud de orden con items
        OrderRequest.OrderItemRequest itemRequest = new OrderRequest.OrderItemRequest();
        itemRequest.setProductoId(101L);
        itemRequest.setCantidad(5);
        itemRequest.setPrecio(15.0);

        OrderRequest request = new OrderRequest();
        request.setItems(List.of(itemRequest));

        // Simular excepción al validar stock
        doThrow(new RuntimeException("Stock insuficiente"))
                .when(productoClient).validarStock(any());

        // Ejecutar y verificar excepción
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderServiceImpl.crearOrden(userEmail, request)
        );

        assertEquals("Stock insuficiente", exception.getMessage());
        verify(productoClient, times(1)).validarStock(any());
        verify(productoClient, never()).actualizarStock(anyLong(), anyInt()); // Nunca debe intentar actualizar
        verify(orderRepository, never()).save(any(Order.class)); // No debe guardar orden
    }

    @Test
    void testCrearOrden_CuandoFallaActualizacionStock_DeberiaLanzarExcepcion() {
        String userEmail = "cliente@example.com";

        UserResponseDTO usuario = new UserResponseDTO();
        usuario.setUsuarioId(1L);
        when(usuarioClient.obtenerUsuarioPorEmail(userEmail)).thenReturn(usuario);

        OrderRequest.OrderItemRequest itemRequest = new OrderRequest.OrderItemRequest();
        itemRequest.setProductoId(101L);
        itemRequest.setCantidad(2);
        itemRequest.setPrecio(10.0);

        OrderRequest request = new OrderRequest();
        request.setItems(List.of(itemRequest));

        doThrow(new RuntimeException("Error")).when(productoClient).actualizarStock(anyLong(), anyInt());


        // Aquí corregido: aseguramos que se usa en método void
        doThrow(new RuntimeException("Error al actualizar stock"))
                .when(productoClient).actualizarStock(eq(101L), eq(2));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> orderServiceImpl.crearOrden(userEmail, request)
        );

        assertEquals("Error al actualizar stock", exception.getMessage());

        verify(productoClient).validarStock(any());
        verify(productoClient).actualizarStock(101L, 2);
        verify(orderRepository, never()).save(any(Order.class));
    }


    @Test
    void testListarOrdenesPorUsuario_DeberiaRetornarOrdenesDelUsuario() {
        String email = "cliente@example.com";

        // Simular usuario
        UserResponseDTO usuario = new UserResponseDTO();
        usuario.setUsuarioId(1L);
        when(usuarioClient.obtenerUsuarioPorEmail(email)).thenReturn(usuario);

        // Simular orden asociada al usuario
        Order orden = new Order();
        orden.setId(1L);
        orden.setUsuarioId(1L);
        orden.setEstado(OrderStatus.PENDIENTE);
        orden.setFechaCreacion(LocalDateTime.now());
        orden.setTotal(100.0);
        orden.setDetalles(List.of());

        when(orderRepository.findByUsuarioId(1L)).thenReturn(List.of(orden));

        // Ejecutar
        List<OrderResponse> respuestas = orderServiceImpl.listarOrdenesPorUsuario(email);

        // Verificar
        assertNotNull(respuestas);
        assertEquals(1, respuestas.size());
        assertEquals(1L, respuestas.get(0).getId());
        assertEquals(OrderStatus.PENDIENTE, respuestas.get(0).getStatus());

        verify(usuarioClient).obtenerUsuarioPorEmail(email);
        verify(orderRepository).findByUsuarioId(1L);
    }

    @Test
    void testListarOrdenesPorUsuario_UsuarioNoEncontrado_DeberiaLanzarExcepcion() {
        String email = "inexistente@example.com";

        // UsuarioClient devuelve null
        when(usuarioClient.obtenerUsuarioPorEmail(email)).thenReturn(null);

        // Ejecutar y verificar excepción
        assertThrows(NullPointerException.class, () -> {
            orderServiceImpl.listarOrdenesPorUsuario(email);
        });

        verify(usuarioClient).obtenerUsuarioPorEmail(email);
        verify(orderRepository, never()).findByUsuarioId(anyLong());
    }

}
