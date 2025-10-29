package service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import model.OrderSide;
import model.dto.OrderDto;
import lombok.extern.slf4j.Slf4j;
import model.entity.Asset;
import model.entity.Customer;
import model.mapper.OrderMapper;
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
        Customer customer = customerRepository.findById(orderDto.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found with id: " + orderDto.getCustomerId()));

        Asset asset = assetRepository.findByCustomerIdAndAssetNameWithLock(orderDto.getCustomerId(), orderDto.getAssetName())
                .orElseThrow(() -> new EntityNotFoundException("Asset not found with name: " + orderDto.getAssetName()));


        if (orderDto.getOrderSide().equals(OrderSide.BUY.name())) {
            if (asset.getUsableSize().intValue() - orderDto.getSize().intValue() <= 0) {
                throw new IllegalArgumentException("asset usable size not enough");
            }
        }

        Asset tryAsset = assetRepository.findByCustomerIdAndAssetNameWithLock(orderDto.getCustomerId(), "TRY")
                .orElseThrow(() -> new EntityNotFoundException("TRY Asset not found with name: " + orderDto.getAssetName()));

        if (orderDto.getOrderSide().equals(OrderSide.BUY.name())) {
            if (tryAsset.getUsableSize().subtract(orderDto.getSize().multiply(orderDto.getPrice())).compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("try not enough");
            }
        }

        Order order = orderMapper.toEntity(orderDto);
        order.setCustomerId(customer.getId());
        order.setAssetName(asset.getAssetName());
        order.setStatus(OrderStatus.PENDING);
        order.setCreateDate(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);

        log.info("Order created successfully with id={}, type={}, asset={}",
                savedOrder.getId(), savedOrder.getOrderSide(), savedOrder.getAssetName());

        if (orderDto.getOrderSide().equals(OrderSide.BUY.name())) {
            asset.setUsableSize(asset.getUsableSize().subtract(orderDto.getSize()));
        }
        else {
            asset.setUsableSize(asset.getUsableSize().add(orderDto.getSize()));
        }
        assetRepository.save(asset);

        if (orderDto.getOrderSide().equals(OrderSide.BUY.name())) {
            tryAsset.setUsableSize(tryAsset.getUsableSize().subtract(orderDto.getSize()));
        }
        else {
            tryAsset.setUsableSize(tryAsset.getUsableSize().add(orderDto.getSize()));
        }
        assetRepository.save(tryAsset);

        return orderMapper.toDto(savedOrder);
    }

    @Override
    public void cancelOrder(Long customerId, Long orderId) {
        orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
        orderRepository.deleteById(orderId);
    }

    @Override
    public List<OrderDto> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId)
                .stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void matchOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));

        Asset tryAsset = assetRepository
                .findByCustomerIdAndAssetNameWithLock(order.getCustomerId(), "TRY")
                .orElseThrow(() -> new IllegalStateException("TRY asset not found"));

        tryAsset.setSize(tryAsset.getUsableSize());
        assetRepository.save(tryAsset);

        Asset purchasedAsset = assetRepository
                .findByCustomerIdAndAssetNameWithLock(order.getCustomerId(), order.getAssetName())
                .orElseThrow(() -> new IllegalStateException("asset not found"));

        purchasedAsset.setSize(purchasedAsset.getUsableSize());
        assetRepository.save(purchasedAsset);

        log.info("Order {} matched successfully", orderId);
    }
}