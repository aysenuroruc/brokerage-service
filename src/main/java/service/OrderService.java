package service;

import model.dto.OrderDto;
import java.util.List;

public interface OrderService {
    OrderDto getOrderById(Long orderId);
    OrderDto createOrder(OrderDto orderDto);
    void cancelOrder(Long customerId, Long orderId);
    List<OrderDto> getOrdersByCustomer(Long customerId);
    void matchOrder(Long orderId);
}
