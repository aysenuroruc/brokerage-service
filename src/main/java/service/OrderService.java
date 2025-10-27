package service;

import model.dto.OrderDto;

import java.util.List;

public interface OrderService {
    OrderDto createOrder(OrderDto orderDto);

    List<OrderDto> getOrdersByCustomer(Long customerId);

    OrderDto getOrderById(Long orderId);
}
