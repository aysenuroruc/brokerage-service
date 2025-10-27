package controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.dto.OrderDto;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import service.ExchangeMatchingService;
import service.ExchangeQueueService;
import service.OrderService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ExchangeQueueService exchangeQueueService;
    private final ExchangeMatchingService exchangeMatchingService;

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@RequestBody OrderDto orderDto) {
        log.info("Creating new order: {}", orderDto);

        OrderDto savedOrder = orderService.createOrder(orderDto);
        exchangeQueueService.enqueueOrder(savedOrder.getAssetName(), savedOrder.getId());

        return ResponseEntity.ok(savedOrder);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderDto>> getOrdersByCustomer(@PathVariable Long customerId) {
        log.info("Fetching orders for customerId={}", customerId);
        return ResponseEntity.ok(orderService.getOrdersByCustomer(customerId));
    }

    /**
     * Admin endpoint: Starts matching for all assets.
     */
    @PostMapping("/match/all")
    public ResponseEntity<String> matchAllOrders() {
        log.warn("Manual matching triggered for ALL assets.");
        exchangeMatchingService.matchAllAssets();
        return ResponseEntity.ok("All asset matching triggered successfully.");
    }

    /**
     * Admin endpoint: Initiates matching for a specific asset.
     */
    @PostMapping("/match/{assetName}")
    public ResponseEntity<String> matchOrdersByAsset(@PathVariable String assetName) {
        log.warn("Manual matching triggered for asset={}", assetName);
        exchangeMatchingService.matchAsset(assetName);
        return ResponseEntity.ok("Matching triggered for asset: " + assetName);
    }

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("OrderController is up and running 🚀");
    }
}
