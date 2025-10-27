package service.impl;

import org.springframework.stereotype.Service;
import service.ExchangeQueueService;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class ExchangeQueueServiceImpl implements ExchangeQueueService {
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<Long>> queues = new ConcurrentHashMap<>();

    @Override
    public void enqueueOrder(String assetName, Long orderId) {
        queues.computeIfAbsent(assetName, k -> new ConcurrentLinkedQueue<>()).add(orderId);
    }

    @Override
    public Long pollOrder(String assetName) {
        var q = queues.get(assetName);
        if (q == null) return null;
        return q.poll();
    }

    @Override
    public boolean hasOrders(String assetName) {
        var q = queues.get(assetName);
        return q != null && !q.isEmpty();
    }

    @Override
    public Set<String> listAssetKeys() {
        return queues.keySet();
    }
}