package com.tienda.ordenes.service;

import com.tienda.ordenes.model.Order;
import com.tienda.ordenes.dto.OrderResponse;
import com.tienda.ordenes.dto.UserResponseDTO;

public interface PagoService {

    OrderResponse procesarPago(Order order, UserResponseDTO usuario);

    
}
    
