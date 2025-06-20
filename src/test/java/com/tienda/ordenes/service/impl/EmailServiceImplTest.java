package com.tienda.ordenes.service.impl;

import com.tienda.ordenes.dto.OrderResponse;
import com.tienda.ordenes.model.OrderStatus;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.MailSendException;


import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private com.tienda.ordenes.repository.OrderRepository orderRepository;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    void testEnviarConfirmacionPago_DeberiaEnviarCorreoExitosamente() throws Exception {
        String email = "cliente@example.com";
        String nombre = "Juan";
        String orderId = "123";

        OrderResponse orden = OrderResponse.builder()
                .id(123L)
                .status(OrderStatus.PAGADA)
                .fechaCreacion(LocalDateTime.now())
                .total(200.0)
                .items(List.of())
                .build();

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Ejecutar
        emailService.enviarConfirmacionPago(email, nombre, orderId, orden);

        // Verificar
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(Mockito.<MimeMessage>any());
    }

   @Test
    void testEnviarConfirmacionPago_CuandoFallaElEnvio_DeberiaRegistrarError() throws Exception {
        String email = "fallo@example.com";
        String nombre = "Error";
        String orderId = "999";

        OrderResponse orden = OrderResponse.builder()
                .id(999L)
                .status(OrderStatus.PAGADA)
                .fechaCreacion(LocalDateTime.now())
                .total(150.0)
                .items(List.of())
                .build();

        MimeMessage mensaje = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mensaje);

        // ✅ Lanza RuntimeException que envuelve la MessagingException
        doThrow(new RuntimeException(new MessagingException("Fallo intencional")))
                .when(mailSender).send(any(MimeMessage.class));

        // Ejecutar
        emailService.enviarConfirmacionPago(email, nombre, orderId, orden);

        // Verificar que se intentó enviar el mensaje
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }


    @Test
    void testProcesarPagoYaPagado_NoHaceNada() {
        var orden = new com.tienda.ordenes.model.Order();
        orden.setId(1L);
        orden.setEstado(OrderStatus.PAGADA);

        var usuario = new com.tienda.ordenes.dto.UserResponseDTO();
        usuario.setCorreo("correo@ejemplo.com");
        usuario.setNombre("Usuario Test");

        OrderResponse resultado = emailService.procesarPago(orden, usuario);

        verify(orderRepository, never()).save(any());
        verify(mailSender, never()).send(any(MimeMessage.class));
        assertEquals(OrderStatus.PAGADA, resultado.getStatus());
    }

    @Test
    void testProcesarPagoOrdenPendiente_ProcesaYEnviaCorreo() throws MessagingException {
        // Arrange
        var orden = new com.tienda.ordenes.model.Order();
        orden.setId(1L);
        orden.setEstado(OrderStatus.PENDIENTE);
        orden.setFechaCreacion(LocalDateTime.now());
        orden.setTotal(150.0);
        orden.setDetalles(List.of());

        var usuario = new com.tienda.ordenes.dto.UserResponseDTO();
        usuario.setCorreo("correo@ejemplo.com");
        usuario.setNombre("Usuario Test");

        // Mock para el correo
        MimeMessage mensaje = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mensaje);

        // Act
        OrderResponse resultado = emailService.procesarPago(orden, usuario);

        // Assert
        verify(orderRepository).save(orden);
        verify(mailSender).send(any(MimeMessage.class));
        assertEquals(OrderStatus.PAGADA, resultado.getStatus());
    }



}


