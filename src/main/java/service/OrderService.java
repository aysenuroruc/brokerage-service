package service;

import dto.OrderDto;

import java.util.List;

public interface OrderService {
    OrderDto createOrder(OrderDto orderDto);

    List<OrderDto> listOrdersByCustomer(Long customerId);

    void cancelOrder(Long orderId);
}
