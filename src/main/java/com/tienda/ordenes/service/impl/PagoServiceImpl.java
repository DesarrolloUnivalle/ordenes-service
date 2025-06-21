package com.tienda.ordenes.service.impl;

import com.tienda.ordenes.dto.UserResponseDTO;
import com.tienda.ordenes.dto.OrderResponse;
import com.tienda.ordenes.model.Order;
import com.tienda.ordenes.model.OrderStatus;
import com.tienda.ordenes.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;


@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements com.tienda.ordenes.service.PagoService {

    private final OrderRepository orderRepository;
    private final JavaMailSender mailSender;

    @Override
    public OrderResponse procesarPago(Order order, UserResponseDTO usuario) {
        if (order.getEstado() == OrderStatus.PAGADA) {
            return OrderResponse.fromEntity(order);
        }

        order.setEstado(OrderStatus.PAGADA);
        orderRepository.save(order);

        try {
            var mimeMessage = mailSender.createMimeMessage();
            mimeMessage.setSubject("Confirmación de pago - Orden #" + order.getId());
            mimeMessage.setRecipients(MimeMessage.RecipientType.TO, usuario.getCorreo());
            mimeMessage.setText("Hola " + usuario.getNombre() + ", tu pago ha sido procesado exitosamente.");
            mailSender.send(mimeMessage);
        } catch (Exception e) {
            // Loguea o maneja el error, pero no interrumpas el flujo
            System.err.println("Error al enviar correo: " + e.getMessage());
        }

        return OrderResponse.fromEntity(order);
    }

}
