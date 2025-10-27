package service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import model.dto.OrderDto;
import lombok.extern.slf4j.Slf4j;
import mapper.OrderMapper;
import model.OrderStatus;
import model.entity.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.AssetRepository;
import repository.CustomerRepository;
import repository.OrderRepository;
import service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final AssetRepository assetRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public OrderDto createOrder(OrderDto orderDto) {
        log.info("Creating new order for customerId={}, asset={}", orderDto.getCustomerId(), orderDto.getAssetName());

        // Müşteri ve asset doğrulaması
        var customer = customerRepository.findById(orderDto.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + orderDto.getCustomerId()));

        var asset = assetRepository.findByAssetName(orderDto.getAssetName())
                .orElseThrow(() -> new EntityNotFoundException("Asset not found with name: " + orderDto.getAssetName()));

        Order order = orderMapper.toEntity(orderDto);
        order.setCustomerId(customer.getId());
        order.setAssetName(asset.getAssetName());
        order.setStatus(OrderStatus.PENDING);
        order.setCreateDate(LocalDateTime.now());

        // Fiyat validasyonu
        if (order.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Order price must be greater than 0");
        }

        Order savedOrder = orderRepository.save(order);

        log.info("Order created successfully with id={}, type={}, asset={}",
                savedOrder.getId(), savedOrder.getOrderSide(), savedOrder.getAssetName());

        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByCustomer(Long customerId) {
        log.debug("Fetching all orders for customerId={}", customerId);
        return orderRepository.findByCustomerId(customerId)
                .stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .map(orderMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
    }
}
