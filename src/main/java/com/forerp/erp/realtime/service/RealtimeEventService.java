package com.forerp.erp.realtime.service;

import com.forerp.erp.realtime.dto.RealtimeEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RealtimeEventService {

    private static final long SSE_TIMEOUT_MS = 60L * 60L * 1000L;

    private final ConcurrentHashMap<String, Subscriber> subscribers = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long storeId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        String id = UUID.randomUUID().toString();
        subscribers.put(id, new Subscriber(emitter, storeId));

        emitter.onCompletion(() -> subscribers.remove(id));
        emitter.onTimeout(() -> subscribers.remove(id));
        emitter.onError(ex -> subscribers.remove(id));

        send(id, "connected", new RealtimeEvent(
                "connected",
                storeId,
                LocalDateTime.now(),
                Map.of("message", "SSE connected")
        ));

        return emitter;
    }

    public void publishInventoryChanged(Long storeId, String reason) {
        publish("inventory.changed", storeId, Map.of("reason", reason));
    }

    public void publishOrderChanged(Long storeId, Long orderId, String reason) {
        publish("order.changed", storeId, Map.of("orderId", orderId, "reason", reason));
    }

    public void publishPaymentChanged(Long storeId, Long paymentId, String reason) {
        publish("payment.changed", storeId, Map.of("paymentId", paymentId, "reason", reason));
    }

    public void publish(String type, Long storeId, Map<String, Object> payload) {
        Runnable task = () -> publishNow(type, storeId, payload);
        if (TransactionSynchronizationManager.isActualTransactionActive()
                && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
            return;
        }
        task.run();
    }

    private void publishNow(String type, Long storeId, Map<String, Object> payload) {
        RealtimeEvent event = new RealtimeEvent(type, storeId, LocalDateTime.now(), payload);
        for (Map.Entry<String, Subscriber> entry : subscribers.entrySet()) {
            Subscriber subscriber = entry.getValue();
            if (subscriber.storeId() != null && storeId != null && !subscriber.storeId().equals(storeId)) {
                continue;
            }
            send(entry.getKey(), type, event);
        }
    }

    private void send(String id, String eventName, RealtimeEvent event) {
        Subscriber subscriber = subscribers.get(id);
        if (subscriber == null) {
            return;
        }

        try {
            subscriber.emitter().send(
                    SseEmitter.event()
                            .id(UUID.randomUUID().toString())
                            .name(eventName)
                            .data(event)
            );
        } catch (IOException | IllegalStateException ex) {
            subscribers.remove(id);
        }
    }

    private record Subscriber(SseEmitter emitter, Long storeId) {
    }
}
