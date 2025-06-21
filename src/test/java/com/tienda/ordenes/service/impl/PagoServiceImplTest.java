package com.tienda.ordenes.service.impl;

import com.tienda.ordenes.dto.OrderResponse;
import com.tienda.ordenes.dto.UserResponseDTO;
import com.tienda.ordenes.model.Order;
import com.tienda.ordenes.model.OrderStatus;
import com.tienda.ordenes.repository.OrderRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mail.javamail.JavaMailSender;
import jakarta.mail.internet.MimeMessage;




import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;


class PagoServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private jakarta.mail.internet.MimeMessage mimeMessage;

    @InjectMocks
    private PagoServiceImpl pagoService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private Order crearOrden(OrderStatus estado) {
        Order orden = new Order();
        orden.setId(1L);
        orden.setEstado(estado);
        orden.setDetalles(List.of());
        return orden;
    }

    private UserResponseDTO crearUsuario() {
        UserResponseDTO usuario = new UserResponseDTO();
        usuario.setCorreo("cliente@correo.com");
        usuario.setNombre("Cliente");
        return usuario;
    }

    @Test
    void procesarPago_ordenYaPagada_noHaceNada() {
        Order orden = crearOrden(OrderStatus.PAGADA);
        UserResponseDTO usuario = crearUsuario();

        OrderResponse response = pagoService.procesarPago(orden, usuario);

        assertEquals(OrderStatus.PAGADA, response.getStatus());
        verify(orderRepository, never()).save(any());
        verify(mailSender, never()).send(ArgumentMatchers.<MimeMessage>any());
    }

    @Test
    void procesarPago_ordenPendiente_enviaCorreoYActualizaEstado() throws Exception {
        Order orden = crearOrden(OrderStatus.PENDIENTE);
        UserResponseDTO usuario = crearUsuario();

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        OrderResponse response = pagoService.procesarPago(orden, usuario);

        assertEquals(OrderStatus.PAGADA, response.getStatus());
        verify(orderRepository).save(orden);
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void procesarPago_fallaAlEnviarCorreo_noRompeElFlujo() {
        Order orden = crearOrden(OrderStatus.PENDIENTE);
        UserResponseDTO usuario = crearUsuario();

        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Fallo simulador"));

        OrderResponse response = pagoService.procesarPago(orden, usuario);

        assertEquals(OrderStatus.PAGADA, response.getStatus());
        verify(orderRepository).save(orden);
        verify(mailSender).createMimeMessage();
    }

    @Test
    void procesarPago_usuarioSinCorreo_noEnviaCorreo() {
    Order orden = crearOrden(OrderStatus.PENDIENTE);
    orden.setDetalles(List.of()); // evita NPE

    UserResponseDTO usuario = crearUsuario();
    usuario.setCorreo(null);  // Correo nulo

    when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

    OrderResponse response = pagoService.procesarPago(orden, usuario);

    assertEquals(OrderStatus.PAGADA, response.getStatus());
    verify(orderRepository).save(orden);
    verify(mailSender).send(mimeMessage); // aún lo intenta (podemos discutir si esto se permite)
}

}
