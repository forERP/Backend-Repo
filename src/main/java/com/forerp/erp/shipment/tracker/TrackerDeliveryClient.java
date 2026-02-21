package com.forerp.erp.shipment.tracker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TrackerDeliveryClient {

    private static final String CARRIER_LIST_QUERY = """
            query CarrierList($first: Int!, $searchText: String, $countryCode: String) {
              carriers(first: $first, searchText: $searchText, countryCode: $countryCode) {
                edges {
                  node {
                    id
                    name
                  }
                }
              }
            }
            """;

    private static final String TRACK_QUERY = """
            query Track($carrierId: ID!, $trackingNumber: String!) {
              track(carrierId: $carrierId, trackingNumber: $trackingNumber) {
                trackingNumber
                lastEvent {
                  time
                  description
                  location {
                    name
                  }
                  status {
                    code
                    name
                  }
                }
                events(last: 20) {
                  edges {
                    node {
                      time
                      description
                      location {
                        name
                      }
                      status {
                        code
                        name
                      }
                    }
                  }
                }
              }
            }
            """;

    private static final String REGISTER_WEBHOOK_MUTATION = """
            mutation RegisterTrackWebhook($input: RegisterTrackWebhookInput!) {
              registerTrackWebhook(input: $input)
            }
            """;

    private final ObjectMapper objectMapper;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Value("${tracker.api.base-url:https://apis.tracker.delivery/graphql}")
    private String trackerApiBaseUrl;

    @Value("${tracker.api.client-id:}")
    private String trackerClientId;

    @Value("${tracker.api.client-secret:}")
    private String trackerClientSecret;

    public boolean isConfigured() {
        return trackerClientId != null && !trackerClientId.isBlank()
                && trackerClientSecret != null && !trackerClientSecret.isBlank();
    }

    public List<TrackerCarrier> getCarriers(String searchText, String countryCode, int first) {
        if (!isConfigured()) {
            return List.of();
        }

        int safeFirst = Math.max(1, Math.min(first, 200));

        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("first", safeFirst);
        variables.put("searchText", normalize(searchText));
        variables.put("countryCode", normalize(countryCode));

        JsonNode data = execute(CARRIER_LIST_QUERY, variables);
        JsonNode edges = data.path("carriers").path("edges");

        if (!edges.isArray() || edges.isEmpty()) {
            return List.of();
        }

        List<TrackerCarrier> carriers = new ArrayList<>();
        for (JsonNode edge : edges) {
            JsonNode node = edge.path("node");
            String id = text(node, "id");
            String name = text(node, "name");
            if (id == null || name == null) {
                continue;
            }
            carriers.add(new TrackerCarrier(id, name));
        }
        return carriers;
    }

    public Optional<TrackerTrackInfo> track(String carrierId, String trackingNumber) {
        if (!isConfigured()) {
            return Optional.empty();
        }

        String normalizedCarrierId = normalize(carrierId);
        String normalizedTrackingNumber = normalize(trackingNumber);
        if (normalizedCarrierId == null || normalizedTrackingNumber == null) {
            return Optional.empty();
        }

        Map<String, Object> variables = Map.of(
                "carrierId", normalizedCarrierId,
                "trackingNumber", normalizedTrackingNumber
        );

        JsonNode data = execute(TRACK_QUERY, variables);

        JsonNode trackNode = data.path("track");
        if (trackNode.isMissingNode() || trackNode.isNull()) {
            return Optional.empty();
        }

        JsonNode lastEvent = trackNode.path("lastEvent");
        String lastCode = text(lastEvent.path("status"), "code");
        String lastName = text(lastEvent.path("status"), "name");
        String lastEventTime = text(lastEvent, "time");
        String lastEventDescription = text(lastEvent, "description");
        String lastEventLocation = text(lastEvent.path("location"), "name");

        List<TrackerTrackEvent> events = new ArrayList<>();
        JsonNode eventEdges = trackNode.path("events").path("edges");
        if (eventEdges.isArray()) {
            for (JsonNode edge : eventEdges) {
                JsonNode node = edge.path("node");
                events.add(new TrackerTrackEvent(
                        text(node.path("status"), "code"),
                        text(node.path("status"), "name"),
                        text(node, "time"),
                        text(node.path("location"), "name"),
                        text(node, "description")
                ));
            }
        }

        return Optional.of(new TrackerTrackInfo(
                normalizedCarrierId,
                normalizedTrackingNumber,
                lastCode,
                lastName,
                lastEventTime,
                lastEventLocation,
                lastEventDescription,
                events
        ));
    }

    public boolean registerTrackWebhook(String carrierId, String trackingNumber, String callbackUrl, LocalDateTime expirationUtc) {
        if (!isConfigured()) {
            return false;
        }

        String normalizedCarrierId = normalize(carrierId);
        String normalizedTrackingNumber = normalize(trackingNumber);
        String normalizedCallbackUrl = normalize(callbackUrl);
        if (normalizedCarrierId == null || normalizedTrackingNumber == null || normalizedCallbackUrl == null || expirationUtc == null) {
            return false;
        }

        String expirationTime = expirationUtc.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        Map<String, Object> input = new LinkedHashMap<>();
        input.put("carrierId", normalizedCarrierId);
        input.put("trackingNumber", normalizedTrackingNumber);
        input.put("callbackUrl", normalizedCallbackUrl);
        input.put("expirationTime", expirationTime);

        Map<String, Object> variables = Map.of("input", input);
        JsonNode data = execute(REGISTER_WEBHOOK_MUTATION, variables);
        return data.path("registerTrackWebhook").asBoolean(false);
    }

    private JsonNode execute(String query, Map<String, Object> variables) {
        String url = normalizeBaseUrl(trackerApiBaseUrl);
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("query", query);
        payload.put("variables", variables == null ? Map.of() : variables);

        String body;
        try {
            body = objectMapper.writeValueAsString(payload);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize tracker request body.", ex);
        }

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", buildAuthorizationValue())
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Tracker API request failed.", ex);
        }

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("Tracker API responded with status=" + response.statusCode());
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(response.body());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse tracker response.", ex);
        }

        JsonNode errors = root.path("errors");
        if (errors.isArray() && !errors.isEmpty()) {
            String message = errors.get(0).path("message").asText("Tracker API error");
            throw new IllegalStateException(message);
        }

        JsonNode data = root.path("data");
        if (data.isMissingNode() || data.isNull()) {
            throw new IllegalStateException("Tracker API data is missing.");
        }
        return data;
    }

    private String buildAuthorizationValue() {
        return "TRACKQL-API-KEY " + trackerClientId.trim() + ":" + trackerClientSecret.trim();
    }

    private String normalizeBaseUrl(String value) {
        if (value == null || value.isBlank()) {
            return "https://apis.tracker.delivery/graphql";
        }
        return value.trim();
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String text(JsonNode node, String field) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        String value = node.path(field).asText(null);
        return normalize(value);
    }

    public record TrackerCarrier(String id, String name) {
    }

    public record TrackerTrackInfo(
            String carrierId,
            String trackingNumber,
            String lastStatusCode,
            String lastStatusName,
            String lastEventTime,
            String lastEventLocation,
            String lastEventDescription,
            List<TrackerTrackEvent> events
    ) {
    }

    public record TrackerTrackEvent(
            String statusCode,
            String statusName,
            String time,
            String location,
            String description
    ) {
    }
}
