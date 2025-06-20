package com.tienda.ordenes.service;

import com.tienda.ordenes.dto.OrderRequest;
import com.tienda.ordenes.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse crearOrden(String userEmail, OrderRequest request);

    List<OrderResponse> listarOrdenesPorUsuario(String userEmail);

    OrderResponse obtenerOrdenPorId(Long id);

    OrderResponse cancelarOrden(Long id);

    OrderResponse pagarOrden(Long id);
}
