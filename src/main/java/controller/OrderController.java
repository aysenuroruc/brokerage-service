package controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.dto.OrderDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import security.SecurityUtil;
import service.OrderService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/{customerId}/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@RequestBody OrderDto orderDto) {
        Long currentCustomer = SecurityUtil.currentCustomerIdOrNull();
        boolean isAdmin = SecurityUtil.isAdmin();

        if (!isAdmin && (currentCustomer == null || !currentCustomer.equals(orderDto.getCustomerId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        OrderDto savedOrder = orderService.createOrder(orderDto);
        return ResponseEntity.ok(savedOrder);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity cancelOrder(@PathVariable Long customerId, @PathVariable Long orderId) {
        Long currentCustomer = SecurityUtil.currentCustomerIdOrNull();
        boolean isAdmin = SecurityUtil.isAdmin();

        OrderDto order = orderService.getOrderById(orderId);
        if (!isAdmin) {
            if (currentCustomer == null || !currentCustomer.equals(order.getCustomerId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        orderService.cancelOrder(customerId, orderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderDto>> getOrdersByCustomer(@PathVariable Long customerId) {
        Long currentCustomer = SecurityUtil.currentCustomerIdOrNull();
        boolean isAdmin = SecurityUtil.isAdmin();
        if (!isAdmin && (currentCustomer == null || !currentCustomer.equals(customerId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }
}
