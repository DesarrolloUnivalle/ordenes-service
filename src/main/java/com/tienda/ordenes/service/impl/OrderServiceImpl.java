package com.tienda.ordenes.service.impl;

import com.tienda.ordenes.client.UsuarioClient;
import com.tienda.ordenes.client.ProductoClient;
import com.tienda.ordenes.dto.OrderRequest;
import com.tienda.ordenes.dto.OrderResponse;
import com.tienda.ordenes.dto.UserResponseDTO;
import com.tienda.ordenes.model.Order;
import com.tienda.ordenes.model.OrderItem;
import com.tienda.ordenes.model.OrderStatus;
import com.tienda.ordenes.repository.OrderRepository;
import com.tienda.ordenes.service.EmailService;
import com.tienda.ordenes.service.OrderService;
import com.tienda.ordenes.dto.OrderItemResponseDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    @Autowired
    private EmailService emailService;
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final UsuarioClient usuarioClient;
    private final ProductoClient productoClient;

    @Override
    public OrderResponse obtenerOrdenPorId(Long orderId) {
        return orderRepository.findById(orderId)
                .map(OrderResponse::fromEntity)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));
    }

    @Override
    public OrderResponse cancelarOrden(Long orderId) {
        Order orden = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        orden.setEstado(OrderStatus.CANCELADA);
        orderRepository.save(orden);

        return OrderResponse.fromEntity(orden);
    }

    
    @Override
    public OrderResponse pagarOrden(Long orderId) {
        Order orden = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        orden.setEstado(OrderStatus.PAGADA);
        orderRepository.save(orden);

        return OrderResponse.fromEntity(orden);
    }

    @Override
    @Transactional
    public OrderResponse crearOrden(String userEmail, OrderRequest request) {
        logger.info("Iniciando creación de orden para el usuario con email: {}", userEmail);

        UserResponseDTO usuario = usuarioClient.obtenerUsuarioPorEmail(userEmail);
        logger.info("Respuesta del servicio de usuarios: {}", usuario);

        if (usuario == null || usuario.getUsuarioId() == null) {
            logger.error("El usuario no existe o no tiene un ID válido.");
            throw new IllegalArgumentException("El usuario no existe o no tiene un ID válido.");
        }

        Order order = new Order();
        order.setUsuarioId(usuario.getUsuarioId());
        logger.info("Asignando usuarioId a la orden: {}", usuario.getUsuarioId());

        order.setFechaCreacion(LocalDateTime.now());
        order.setEstado(OrderStatus.PENDIENTE);

        List<OrderItem> items = request.getItems().stream()
            .map(itemRequest -> {
                OrderItem item = new OrderItem();
                item.setProductoId(itemRequest.getProductoId());
                item.setCantidad(itemRequest.getCantidad());
                item.setPrecio(itemRequest.getPrecio());
                item.setOrden(order);
                return item;
            })
            .collect(Collectors.toList());

        order.setDetalles(items);

        productoClient.validarStock(items);

        for (OrderItem item : items) {
            productoClient.actualizarStock(item.getProductoId(), item.getCantidad());
        }

        Double total = calcularTotal(items);
        order.setTotal(total);
        logger.info("Total calculado para la orden: {}", total);

        Order savedOrder = orderRepository.save(order);
        logger.info("Orden guardada con ID: {}", savedOrder.getId());

        // 🔧 Convertir List<OrderItem> → List<OrderItemResponseDTO>
        List<OrderItemResponseDTO> itemDTOs = savedOrder.getDetalles().stream()
            .map(i -> new OrderItemResponseDTO(i.getProductoId(), i.getCantidad(), i.getPrecio()))
            .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(savedOrder.getId())
                .status(savedOrder.getEstado())
                .fechaCreacion(savedOrder.getFechaCreacion())
                .total(savedOrder.getTotal())
                .items(itemDTOs)
                .build();
    }


    @Override
    public List<OrderResponse> listarOrdenesPorUsuario(String userEmail) {
        logger.info("Listando órdenes para el usuario con email: {}", userEmail);

        UserResponseDTO usuario = usuarioClient.obtenerUsuarioPorEmail(userEmail);
        logger.info("Respuesta del servicio de usuarios: {}", usuario);

        return orderRepository.findByUsuarioId(usuario.getUsuarioId()).stream()
                .map(order -> {
                    List<OrderItemResponseDTO> itemDTOs = order.getDetalles().stream()
                            .map(i -> new OrderItemResponseDTO(i.getProductoId(), i.getCantidad(), i.getPrecio()))
                            .collect(Collectors.toList());

                    return OrderResponse.builder()
                            .id(order.getId())
                            .status(order.getEstado())
                            .fechaCreacion(order.getFechaCreacion())
                            .total(order.getTotal())
                            .items(itemDTOs)
                            .build();
                })
                .collect(Collectors.toList());
    }


    private Double calcularTotal(List<OrderItem> items) {
        return items.stream()
                .mapToDouble(item -> item.getPrecio() * item.getCantidad())
                .sum();
    }

    public void procesarPago(Order order, UserResponseDTO usuario) {
        String emailUsuario = usuario.getCorreo();
        String nombreUsuario = usuario.getNombre();

        if (order.getEstado() == OrderStatus.PAGADA) {
            logger.info("La orden ya está pagada. No se requiere procesamiento adicional.");
            return;
        }
        order.setEstado(OrderStatus.PAGADA);
        orderRepository.save(order);
        logger.info("Orden procesada y marcada como PAGADA.");
        
        
        OrderResponse respuesta = OrderResponse.fromEntity(order);
        emailService.enviarConfirmacionPago(emailUsuario, nombreUsuario, order.getId().toString(), respuesta);


    }
}