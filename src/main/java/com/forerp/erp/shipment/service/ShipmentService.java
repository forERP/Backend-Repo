package com.forerp.erp.shipment.service;

import com.forerp.erp.common.query.QueryParamParser;
import com.forerp.erp.outbound.domain.Outbound;
import com.forerp.erp.outbound.domain.OutboundStatus;
import com.forerp.erp.order.domain.OrderStatus;
import com.forerp.erp.realtime.service.RealtimeEventService;
import com.forerp.erp.shipment.domain.Shipment;
import com.forerp.erp.shipment.domain.ShipmentFlowType;
import com.forerp.erp.shipment.domain.ShipmentStatus;
import com.forerp.erp.shipment.dto.ShipmentCarrierResponse;
import com.forerp.erp.shipment.dto.ShipmentListResponse;
import com.forerp.erp.shipment.dto.ShipmentTrackingResponse;
import com.forerp.erp.shipment.repository.ShipmentRepository;
import com.forerp.erp.shipment.tracker.TrackerDeliveryClient;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.storeproduct.domain.StoreProduct;
import com.forerp.erp.warehouse.domain.Warehouse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ShipmentService {

    private static final Logger log = LoggerFactory.getLogger(ShipmentService.class);

    private final ShipmentRepository shipmentRepository;
    private final TrackerDeliveryClient trackerDeliveryClient;
    private final RealtimeEventService realtimeEventService;

    @Value("${tracker.api.default-country-code:KR}")
    private String trackerDefaultCountryCode;

    @Value("${tracker.webhook.enabled:true}")
    private boolean trackerWebhookEnabled;

    @Value("${tracker.webhook.base-url:}")
    private String trackerWebhookBaseUrl;

    @Value("${tracker.webhook.secret:}")
    private String trackerWebhookSecret;

    @Value("${tracker.webhook.expiration-hours:48}")
    private int trackerWebhookExpirationHours;

    @Transactional(readOnly = true)
    public List<ShipmentCarrierResponse> listCarriers(String searchText, Integer size) {
        int fetchSize = size == null ? 100 : Math.max(1, Math.min(size, 200));
        String countryCode = normalize(trackerDefaultCountryCode);

        return trackerDeliveryClient.getCarriers(searchText, countryCode, fetchSize).stream()
                .map(carrier -> new ShipmentCarrierResponse(carrier.id(), carrier.name()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ShipmentListResponse listShipments(
            String flowType,
            Long storeId,
            Long warehouseId,
            String shipmentStatus,
            String from,
            String to,
            int page,
            int size
    ) {
        ShipmentFlowType parsedFlowType = QueryParamParser.parseEnumOrNull(flowType, ShipmentFlowType.class, "flowType");
        ShipmentStatus parsedStatus = QueryParamParser.parseEnumOrNull(shipmentStatus, ShipmentStatus.class, "shipmentStatus");
        LocalDateTime fromDt = QueryParamParser.parseFromDate(from);
        LocalDateTime toDt = QueryParamParser.parseToDateExclusive(to);
        boolean includeInbound = parsedFlowType == null || parsedFlowType == ShipmentFlowType.INBOUND;
        boolean includeOutbound = parsedFlowType == null || parsedFlowType == ShipmentFlowType.OUTBOUND;

        Page<Shipment> result = shipmentRepository.search(
                includeInbound,
                includeOutbound,
                storeId,
                warehouseId,
                parsedStatus,
                fromDt,
                toDt,
                PageRequest.of(page, size)
        );

        List<ShipmentListResponse.ShipmentListItem> content = result.getContent().stream()
                .map(this::toListItem)
                .toList();

        return new ShipmentListResponse(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    public ShipmentTrackingResponse getTracking(Long shipmentId, boolean syncStatus) {
        Shipment shipment = loadShipment(shipmentId);
        ShipmentFlowType flowType = resolveFlowType(shipment);

        String carrierCode = normalize(shipment.getCarrierCode());
        String trackingNumber = normalize(shipment.getTrackingNumber());
        if (carrierCode == null || trackingNumber == null) {
            return new ShipmentTrackingResponse(
                    shipment.getId(),
                    flowType.name(),
                    shipment.getStatus().name(),
                    shipment.getCarrierCode(),
                    shipment.getCarrier(),
                    shipment.getTrackingNumber(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    false,
                    false,
                    "??? ?? ?? ????? ?? ?? ??? ??? ? ????.",
                    List.of()
            );
        }

        Optional<TrackerDeliveryClient.TrackerTrackInfo> tracked;
        try {
            tracked = trackerDeliveryClient.track(carrierCode, trackingNumber);
        } catch (Exception ex) {
            log.warn("Tracker ?? ??. shipmentId={}", shipment.getId(), ex);
            return new ShipmentTrackingResponse(
                    shipment.getId(),
                    flowType.name(),
                    shipment.getStatus().name(),
                    shipment.getCarrierCode(),
                    shipment.getCarrier(),
                    shipment.getTrackingNumber(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    false,
                    false,
                    "???? ? ??? ??????.",
                    List.of()
            );
        }

        if (tracked.isEmpty()) {
            return new ShipmentTrackingResponse(
                    shipment.getId(),
                    flowType.name(),
                    shipment.getStatus().name(),
                    shipment.getCarrierCode(),
                    shipment.getCarrier(),
                    shipment.getTrackingNumber(),
                    null,
                    null,
                    null,
                    null,
                    null,
                    false,
                    false,
                    "?? ??? ?? ??? ????.",
                    List.of()
            );
        }

        TrackerDeliveryClient.TrackerTrackInfo info = tracked.get();

        boolean localStatusChanged = false;
        if (syncStatus) {
            localStatusChanged = applyTrackingResult(shipment, info);
            if (localStatusChanged) {
                publishShipmentChanged(shipment, flowType, "tracker_sync");
            }
        }

        List<ShipmentTrackingResponse.TrackingEventItem> events = info.events() == null
                ? List.of()
                : info.events().stream()
                .map(event -> new ShipmentTrackingResponse.TrackingEventItem(
                        event.statusCode(),
                        event.statusName(),
                        event.time(),
                        event.location(),
                        event.description()
                ))
                .toList();

        return new ShipmentTrackingResponse(
                shipment.getId(),
                flowType.name(),
                shipment.getStatus().name(),
                shipment.getCarrierCode(),
                shipment.getCarrier(),
                shipment.getTrackingNumber(),
                info.lastStatusCode(),
                info.lastStatusName(),
                info.lastEventTime(),
                info.lastEventLocation(),
                info.lastEventDescription(),
                "DELIVERED".equalsIgnoreCase(info.lastStatusCode()),
                localStatusChanged,
                null,
                events
        );
    }

    public void registerWebhookIfPossible(Shipment shipment) {
        if (shipment == null || !trackerWebhookEnabled) {
            return;
        }

        String carrierCode = normalize(shipment.getCarrierCode());
        String trackingNumber = normalize(shipment.getTrackingNumber());
        String callbackUrl = buildCallbackUrl();

        if (carrierCode == null || trackingNumber == null || callbackUrl == null) {
            return;
        }

        int expireHours = Math.max(1, Math.min(trackerWebhookExpirationHours, 168));
        LocalDateTime expirationUtc = LocalDateTime.now().plusHours(expireHours);

        try {
            boolean registered = trackerDeliveryClient.registerTrackWebhook(carrierCode, trackingNumber, callbackUrl, expirationUtc);
            if (!registered) {
                log.warn("Tracker webhook registration returned false. shipmentId={}", shipment.getId());
            }
        } catch (Exception ex) {
            log.warn("Tracker webhook registration failed. shipmentId={}", shipment.getId(), ex);
        }
    }

    public void handleTrackerWebhook(String token, String carrierId, String trackingNumber) {
        String configuredSecret = normalize(trackerWebhookSecret);
        if (trackerWebhookEnabled && configuredSecret != null && !configuredSecret.equals(normalize(token))) {
            throw new IllegalArgumentException("???? ?? webhook token ???.");
        }

        String normalizedCarrierId = normalize(carrierId);
        String normalizedTrackingNumber = normalize(trackingNumber);
        if (normalizedCarrierId == null || normalizedTrackingNumber == null) {
            return;
        }

        Shipment shipment = shipmentRepository.findByCarrierCodeAndTrackingNumber(normalizedCarrierId, normalizedTrackingNumber)
                .orElseGet(() -> shipmentRepository.findFirstByCarrierAndTrackingNumber(normalizedCarrierId, normalizedTrackingNumber)
                        .orElse(null));

        if (shipment == null) {
            return;
        }

        Optional<TrackerDeliveryClient.TrackerTrackInfo> tracked;
        try {
            tracked = trackerDeliveryClient.track(normalizedCarrierId, normalizedTrackingNumber);
        } catch (Exception ex) {
            log.warn("Tracker webhook ? ?? ??. shipmentId={}", shipment.getId(), ex);
            return;
        }

        if (tracked.isEmpty()) {
            return;
        }

        ShipmentFlowType flowType = resolveFlowType(shipment);
        boolean changed = applyTrackingResult(shipment, tracked.get());
        if (changed) {
            publishShipmentChanged(shipment, flowType, "tracker_webhook");
        }
    }

    private ShipmentListResponse.ShipmentListItem toListItem(Shipment shipment) {
        ShipmentFlowType flowType = resolveFlowType(shipment);
        Long inboundId = shipment.getInbound() == null ? null : shipment.getInbound().getId();
        Long outboundId = shipment.getOutbound() == null ? null : shipment.getOutbound().getId();
        Long referenceId = inboundId != null ? inboundId : outboundId;

        Store store = shipment.getInbound() != null
                ? shipment.getInbound().getStore()
                : shipment.getOutbound().getStore();

        Warehouse warehouse = resolveWarehouse(shipment);

        return new ShipmentListResponse.ShipmentListItem(
                shipment.getId(),
                flowType.name(),
                inboundId,
                outboundId,
                referenceId,
                store == null ? null : store.getId(),
                store == null ? null : store.getName(),
                store == null ? null : store.getStoreCode(),
                warehouse == null ? null : warehouse.getId(),
                warehouse == null ? null : warehouse.getName(),
                warehouse == null ? null : warehouse.getCode(),
                shipment.getStatus().name(),
                shipment.getCarrierCode(),
                shipment.getCarrier(),
                shipment.getTrackingNumber(),
                shipment.getCreatedAt(),
                shipment.getDepartedAt(),
                shipment.getArrivedAt()
        );
    }

    private boolean applyTrackingResult(Shipment shipment, TrackerDeliveryClient.TrackerTrackInfo info) {
        String statusCode = normalize(info.lastStatusCode());
        if (statusCode == null) {
            return false;
        }

        ShipmentStatus targetStatus = resolveTargetStatus(statusCode);
        if (targetStatus == null) {
            return false;
        }

        boolean shipmentChanged = false;
        if (targetStatus == ShipmentStatus.SHIPPING) {
            if (shipment.getStatus() == ShipmentStatus.READY) {
                shipment.markShippingFromTracking();
                shipmentChanged = true;
            }
        } else if (targetStatus == ShipmentStatus.ARRIVED) {
            if (shipment.getStatus() == ShipmentStatus.READY) {
                shipment.markShippingFromTracking();
                shipmentChanged = true;
            }
            if (shipment.getStatus() == ShipmentStatus.SHIPPING) {
                shipment.markArrivedFromTracking();
                shipmentChanged = true;
            }

            if (shipment.getOutbound() != null) {
                shipmentChanged = applyOutboundArrivalFromTracking(shipment.getOutbound()) || shipmentChanged;
            }
        }

        return shipmentChanged;
    }

    private boolean applyOutboundArrivalFromTracking(Outbound outbound) {
        if (outbound.getStatus() != OutboundStatus.CONFIRMED && outbound.getStatus() != OutboundStatus.ARRIVED) {
            return false;
        }

        boolean changed = false;
        if (outbound.getStatus() == OutboundStatus.CONFIRMED) {
            outbound.arrive();
            changed = true;
        }

        if (outbound.getOrder().getStatus() != OrderStatus.ARRIVED) {
            outbound.getOrder().markArrived();
            realtimeEventService.publishOrderChanged(outbound.getStore().getId(), outbound.getOrder().getId(), "order_arrived");
            changed = true;
        }

        return changed;
    }

    private ShipmentStatus resolveTargetStatus(String trackerStatusCode) {
        if ("DELIVERED".equalsIgnoreCase(trackerStatusCode)) {
            return ShipmentStatus.ARRIVED;
        }

        List<String> shippingCodes = List.of(
                "UNKNOWN",
                "INFORMATION_RECEIVED",
                "AT_PICKUP",
                "IN_TRANSIT",
                "OUT_FOR_DELIVERY",
                "ATTEMPT_FAIL",
                "AVAILABLE_FOR_PICKUP",
                "EXCEPTION"
        );

        return shippingCodes.stream().anyMatch(code -> code.equalsIgnoreCase(trackerStatusCode))
                ? ShipmentStatus.SHIPPING
                : null;
    }

    private void publishShipmentChanged(Shipment shipment, ShipmentFlowType flowType, String reason) {
        Long storeId = resolveStoreId(shipment);
        if (storeId == null) {
            return;
        }

        realtimeEventService.publish("shipment.changed", storeId, Map.of(
                "shipmentId", shipment.getId(),
                "flowType", flowType.name(),
                "reason", reason
        ));
    }

    private Long resolveStoreId(Shipment shipment) {
        if (shipment.getInbound() != null && shipment.getInbound().getStore() != null) {
            return shipment.getInbound().getStore().getId();
        }
        if (shipment.getOutbound() != null && shipment.getOutbound().getStore() != null) {
            return shipment.getOutbound().getStore().getId();
        }
        return null;
    }

    private Warehouse resolveWarehouse(Shipment shipment) {
        if (shipment.getInbound() != null) {
            return shipment.getInbound().getWarehouse();
        }

        if (shipment.getOutbound() == null || shipment.getOutbound().getItems() == null || shipment.getOutbound().getItems().isEmpty()) {
            return null;
        }

        StoreProduct storeProduct = shipment.getOutbound().getItems().get(0).getStoreProduct();
        if (storeProduct == null) {
            return null;
        }
        return storeProduct.getWarehouse();
    }

    private ShipmentFlowType resolveFlowType(Shipment shipment) {
        if (shipment.getInbound() != null) {
            return ShipmentFlowType.INBOUND;
        }
        return ShipmentFlowType.OUTBOUND;
    }

    private Shipment loadShipment(Long shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("?? ??? ?? ? ????. shipmentId=" + shipmentId));
    }

    private String buildCallbackUrl() {
        String baseUrl = normalize(trackerWebhookBaseUrl);
        if (baseUrl == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder(baseUrl);
        if (builder.charAt(builder.length() - 1) == '/') {
            builder.setLength(builder.length() - 1);
        }

        builder.append("/api/shipments/webhook/tracker");

        String secret = normalize(trackerWebhookSecret);
        if (secret != null) {
            builder.append("?token=")
                    .append(URLEncoder.encode(secret, StandardCharsets.UTF_8));
        }

        return builder.toString();
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
