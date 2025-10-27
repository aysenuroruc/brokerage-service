package service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.OrderStatus;
import model.entity.Asset;
import model.entity.Order;
import model.entity.TransactionLog;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import repository.AssetRepository;
import repository.OrderRepository;
import repository.TransactionLogRepository;
import service.ExchangeMatchingService;
import service.ExchangeQueueService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeMatchingServiceImpl implements ExchangeMatchingService {
    private static final int MAX_RETRIES = 3;
    private final ExchangeQueueService queueService;
    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;
    private final TransactionLogRepository txRepository;

    @Override
    public void matchAllAssets() {
        for (String assetName : queueService.listAssetKeys()) {
            matchAsset(assetName);
        }
    }

    @Override
    public void matchAsset(String assetName) {
        log.info("Matching orders for asset={}", assetName);

        Long orderId;
        while ((orderId = queueService.pollOrder(assetName)) != null) {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null || !"PENDING".equals(order.getStatus())) continue;

            int attempt = 0;
            boolean success = false;

            while (attempt < MAX_RETRIES && !success) {
                attempt++;
                try {
                    processSingleOrderInNewTx(order.getId());
                    success = true;
                } catch (OptimisticLockingFailureException e) {
                    log.warn("Optimistic lock on order {} (attempt {}/{})", order.getId(), attempt, MAX_RETRIES);
                } catch (Exception e) {
                    log.error("Error processing order {}: {}", order.getId(), e.getMessage());
                    markOrderFailed(order, e.getMessage());
                    break;
                }
            }

            if (!success) {
                markOrderFailed(order, "Optimistic lock retry limit reached");
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processSingleOrderInNewTx(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalStateException("Order not found"));

        if ("BUY".equalsIgnoreCase(String.valueOf(order.getOrderSide()))) {
            handleBuyOrder(order);
        } else if ("SELL".equalsIgnoreCase(String.valueOf(order.getOrderSide()))) {
            handleSellOrder(order);
        } else {
            throw new IllegalStateException("Unknown order side: " + order.getOrderSide());
        }

        order.setStatus(OrderStatus.valueOf("MATCHED"));
        orderRepository.save(order);
        writeTxLog(order, "SUCCESS", "Matched successfully");
    }

    private void handleBuyOrder(Order order) {
        Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), "TRY").orElseThrow(() -> new IllegalStateException("TRY asset not found"));
        BigDecimal totalCost = order.getPrice().multiply(order.getSize());

        if (tryAsset.getUsableSize().compareTo(totalCost) < 0)
            throw new IllegalStateException("Insufficient TRY balance");

        tryAsset.setUsableSize(tryAsset.getUsableSize().subtract(totalCost));
        assetRepository.save(tryAsset);

        Asset asset = assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName()).orElseGet(() -> {
            Asset a = new Asset();
            a.setCustomerId(order.getCustomerId());
            a.setAssetName(order.getAssetName());
            a.setSize(BigDecimal.ZERO);
            a.setUsableSize(BigDecimal.ZERO);
            return a;
        });

        asset.setSize(asset.getSize().add(order.getSize()));
        asset.setUsableSize(asset.getUsableSize().add(order.getSize()));
        assetRepository.save(asset);
    }

    private void handleSellOrder(Order order) {
        Asset asset = assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName()).orElseThrow(() -> new IllegalStateException("Asset not found for SELL"));

        if (asset.getUsableSize().compareTo(order.getSize()) < 0)
            throw new IllegalStateException("Insufficient asset size");

        asset.setUsableSize(asset.getUsableSize().subtract(order.getSize()));
        assetRepository.save(asset);

        Asset tryAsset = assetRepository.findByCustomerIdAndAssetName(order.getCustomerId(), "TRY").orElseThrow(() -> new IllegalStateException("TRY asset not found"));

        BigDecimal income = order.getPrice().multiply(order.getSize());
        tryAsset.setUsableSize(tryAsset.getUsableSize().add(income));
        assetRepository.save(tryAsset);
    }

    private void markOrderFailed(Order order, String reason) {
        order.setStatus(OrderStatus.valueOf("FAILED"));
        orderRepository.save(order);
        writeTxLog(order, "FAILED", reason);
    }

    private void writeTxLog(Order order, String result, String message) {
        TransactionLog logEntry = new TransactionLog();
        logEntry.setOrderId(order.getId());
        logEntry.setAssetName(order.getAssetName());
        logEntry.setOrderSide(String.valueOf(order.getOrderSide()));
        logEntry.setSize(order.getSize());
        logEntry.setPrice(order.getPrice());
        logEntry.setResult(result);
        logEntry.setMessage(message);
        logEntry.setTimestamp(LocalDateTime.now());
        txRepository.save(logEntry);
    }
}
