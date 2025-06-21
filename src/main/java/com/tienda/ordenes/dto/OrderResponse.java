package com.tienda.ordenes.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tienda.ordenes.model.Order;
import com.tienda.ordenes.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private Long id;
    private OrderStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaCreacion;

    private Double total;
    private List<OrderItemResponseDTO> items;

    public static OrderResponse fromEntity(Order order) {
        List<OrderItemResponseDTO> itemDTOs = order.getDetalles().stream()
                .map(item -> OrderItemResponseDTO.builder()
                        .productoId(item.getProductoId())
                        .cantidad(item.getCantidad())
                        .precio(item.getPrecio())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getEstado())
                .fechaCreacion(order.getFechaCreacion())
                .total(order.getTotal())
                .items(itemDTOs)
                .build();
    }
}
