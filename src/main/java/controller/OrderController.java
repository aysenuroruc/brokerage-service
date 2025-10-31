package controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.dto.OrderDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import security.SecurityUtil;
import service.OrderService;

import java.net.URI;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/{customerId}/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@PathVariable Long customerId, @RequestBody OrderDto orderDto) {
        Long currentCustomer = SecurityUtil.currentCustomerIdOrNull();
        boolean isAdmin = SecurityUtil.isAdmin();

        if (orderDto.getCustomerId() == null) {
            orderDto.setCustomerId(customerId);
        } else if (!orderDto.getCustomerId().equals(customerId)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (!isAdmin && (currentCustomer == null || !currentCustomer.equals(orderDto.getCustomerId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        OrderDto savedOrder = orderService.createOrder(orderDto);
        URI location = URI.create(String.format("/api/%d/orders/%d", savedOrder.getCustomerId(), savedOrder.getId()));
        return ResponseEntity.created(location).body(savedOrder);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity cancelOrder(@PathVariable Long customerId, @PathVariable Long orderId) {
        Long currentCustomer = SecurityUtil.currentCustomerIdOrNull();
        boolean isAdmin = SecurityUtil.isAdmin();

        if (!isAdmin && (currentCustomer == null || !currentCustomer.equals(customerId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
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
