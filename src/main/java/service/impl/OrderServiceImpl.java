package service.impl;

import dto.OrderDto;
import exception.BusinessException;
import exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import mapper.OrderMapper;
import model.OrderSide;
import model.OrderStatus;
import model.entity.Asset;
import model.entity.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repository.AssetRepository;
import repository.OrderRepository;
import service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private AssetRepository assetRepository;

    @Override
    public OrderDto createOrder(OrderDto orderDto) {

        Asset asset = assetRepository.findByCustomerIdAndAssetName(
                        orderDto.getCustomerId(), orderDto.getAssetName())
                .orElseThrow(() -> new BusinessException("Asset not found for customer"));

        BigDecimal totalValue = orderDto.getPrice().multiply(orderDto.getSize());
        OrderSide side = OrderSide.valueOf(orderDto.getOrderSide().toUpperCase());

        if (side == OrderSide.BUY) {
            Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(orderDto.getCustomerId(), "TRY")
                    .orElseThrow(() -> new BusinessException("TRY asset not found for BUY order"));

            if (tryAsset.getUsableSize().compareTo(totalValue) < 0) {
                throw new BusinessException("Insufficient TRY balance for BUY order");
            }

            tryAsset.setUsableSize(tryAsset.getUsableSize().subtract(totalValue));
            assetRepository.save(tryAsset);
        } else {
            if (asset.getUsableSize().compareTo(orderDto.getSize()) < 0) {
                throw new BusinessException("Insufficient asset size for SELL order");
            }

            asset.setUsableSize(asset.getUsableSize().subtract(orderDto.getSize()));
            assetRepository.save(asset);
        }

        Order order = orderMapper.toEntity(orderDto);
        order.setStatus(OrderStatus.PENDING);
        order.setCreateDate(LocalDateTime.now());
        orderRepository.save(order);

        log.info("Order created successfully. OrderId={}, status={}", order.getId(), order.getStatus());
        return orderMapper.toDto(order);
    }

    @Override
    public List<OrderDto> listOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId)
                .stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Only PENDING orders can be canceled");
        }

        Asset asset = assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName())
                .orElseThrow(() -> new BusinessException("Asset not found for order"));

        // Refund logic
        if (order.getOrderSide() == OrderSide.BUY) {
            Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), "TRY")
                    .orElseThrow(() -> new BusinessException("TRY asset not found during cancel"));
            BigDecimal refundValue = order.getPrice().multiply(order.getSize());
            tryAsset.setUsableSize(tryAsset.getUsableSize().add(refundValue));
            assetRepository.save(tryAsset);
        } else {
            asset.setUsableSize(asset.getUsableSize().add(order.getSize()));
            assetRepository.save(asset);
        }

        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
    }
}
