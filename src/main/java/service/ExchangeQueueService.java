package service;

import java.util.Set;

public interface ExchangeQueueService {
    void enqueueOrder(String assetName, Long orderId);
    Long pollOrder(String assetName);
    boolean hasOrders(String assetName);
    Set<String> listAssetKeys();
}
