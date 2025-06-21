package com.tienda.ordenes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tienda.ordenes.client.UsuarioClient;
import com.tienda.ordenes.dto.ConfirmarPagoRequest;
import com.tienda.ordenes.dto.OrderItemResponseDTO;
import com.tienda.ordenes.dto.OrderRequest;
import com.tienda.ordenes.dto.OrderRequest.OrderItemRequest;
import com.tienda.ordenes.dto.OrderResponse;
import com.tienda.ordenes.dto.UserResponseDTO;
import com.tienda.ordenes.model.Order;
import com.tienda.ordenes.model.OrderItem;
import com.tienda.ordenes.model.OrderStatus;
import com.tienda.ordenes.repository.OrderRepository;
import com.tienda.ordenes.security.JwtAuthFilter;
import com.tienda.ordenes.security.JwtUtil;
import com.tienda.ordenes.service.OrderService;
import com.tienda.ordenes.service.PagoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@WebMvcTest(OrderController.class)
@Import({JwtAuthFilter.class, JwtUtil.class})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderRepository orderRepository;

    @MockBean
    private UsuarioClient usuarioClient;

    @MockBean
    private PagoService pagoService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper.findAndRegisterModules();
    }



    private final RequestPostProcessor jwtToken = jwt().jwt(jwt -> jwt.subject("usuario@example.com"));

    private OrderResponse crearDummyOrdenResponse() {
        OrderItemResponseDTO itemDTO = new OrderItemResponseDTO(1L, 2, 50.0);
        OrderItem item = new OrderItem();
        item.setProductoId(1L);
        item.setCantidad(2);

        return OrderResponse.builder()
                .id(1L)
                .status(OrderStatus.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .total(100.0)
                .items(List.of(itemDTO))
                .build();
    }

    @Test
    void crearOrden_deberiaRetornar201() throws Exception {
    OrderItem item = new OrderItem();
    item.setProductoId(1L);
    item.setCantidad(2);

    Order order = new Order();
    order.setId(1L);
    order.setEstado(OrderStatus.PENDIENTE);
    order.setFechaCreacion(LocalDateTime.now());
    order.setDetalles(List.of(item));

    OrderRequest request = new OrderRequest();
    OrderItemRequest itemRequest = new OrderItemRequest();
    itemRequest.setProductoId(1L);
    itemRequest.setCantidad(2);
    itemRequest.setPrecio(50.0);
    request.setItems(List.of(itemRequest));

    OrderResponse dummy = OrderResponse.fromEntity(order);

    UserResponseDTO usuarioMock = new UserResponseDTO();
    usuarioMock.setUsuarioId(1L);
    usuarioMock.setCorreo("usuario@example.com");
    usuarioMock.setNombre("Juan");
    usuarioMock.setRol("CLIENTE");

    when(usuarioClient.obtenerUsuarioPorEmail(anyString())).thenReturn(usuarioMock);
    when(orderService.crearOrden(anyString(), any(OrderRequest.class))).thenReturn(dummy);

    String jsonRequest = objectMapper.writeValueAsString(request);

    mockMvc.perform(
            post("/api/ordenes")
                .with(jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
        .andDo(result -> {
        System.out.println("RESPONSE: " + result.getResponse().getContentAsString());
        System.out.println("STATUS: " + result.getResponse().getStatus());}) 
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(1L));
}


    @Test
    void listarOrdenesPorUsuario_deberiaRetornarLista() throws Exception {
    UserResponseDTO usuarioMock = new UserResponseDTO();
    usuarioMock.setUsuarioId(1L);
    usuarioMock.setCorreo("usuario@example.com");
    usuarioMock.setNombre("Juan");
    usuarioMock.setRol("CLIENTE");

    when(usuarioClient.obtenerUsuarioPorEmail(anyString())).thenReturn(usuarioMock);
    when(orderService.listarOrdenesPorUsuario("usuario@example.com")).thenReturn(List.of(crearDummyOrdenResponse()));

    mockMvc.perform(get("/api/ordenes")
                .with(jwtToken))
            .andExpect(status().isOk())
            .andDo(result -> {
            System.out.println("RESPONSE: " + result.getResponse().getContentAsString());
            System.out.println("STATUS: " + result.getResponse().getStatus());}) 
            .andExpect(jsonPath("$[0].id").value(1L));       
    }


    @Test
    void confirmarPago_deberiaRetornarOrdenConfirmada() throws Exception {
    Long orderId = 123L;
    String emailUsuario = "cliente@example.com";

    Order order = new Order();
    order.setId(orderId);
    order.setEstado(OrderStatus.PENDIENTE);
    order.setDetalles(List.of());

    UserResponseDTO usuario = new UserResponseDTO();
    usuario.setCorreo(emailUsuario);
    usuario.setNombre("Cliente");

    OrderResponse orderResponse = OrderResponse.fromEntity(order);

    // Mock del repositorio y FeignClient
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
    when(usuarioClient.obtenerUsuarioPorEmail(emailUsuario)).thenReturn(usuario);

    // ✅ Mock del nuevo PagoService
    when(pagoService.procesarPago(order, usuario)).thenReturn(orderResponse);

    ConfirmarPagoRequest request = new ConfirmarPagoRequest();
    request.setOrderId(orderId);
    request.setEmailUsuario(emailUsuario);

    mockMvc.perform(post("/api/ordenes/pago-exitoso")
            .with(jwtToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andDo(result -> System.out.println("RESPONSE: " + result.getResponse().getContentAsString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(orderId));
}

}
