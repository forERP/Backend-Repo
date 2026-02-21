package com.forerp.erp.common.location;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class KakaoAddressGeocodingService {

    private static final Logger log = LoggerFactory.getLogger(KakaoAddressGeocodingService.class);
    private static final String ADDRESS_SEARCH_PATH = "/v2/local/search/address.json";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    @Value("${KAKAO_REST_API_KEY:}")
    private String kakaoRestApiKey;

    @Value("${KAKAO_LOCAL_SEARCH_BASE_URL:https://dapi.kakao.com}")
    private String kakaoLocalSearchBaseUrl;

    public Optional<Coordinates> geocode(String rawAddress) {
        String address = normalize(rawAddress);
        if (address == null) {
            return Optional.empty();
        }
        if (kakaoRestApiKey == null || kakaoRestApiKey.isBlank()) {
            return Optional.empty();
        }

        String requestUrl = buildRequestUrl(address);
        HttpRequest request = HttpRequest.newBuilder(URI.create(requestUrl))
                .GET()
                .header("Authorization", "KakaoAK " + kakaoRestApiKey.trim())
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(5))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Kakao geocoding failed. status={}, address={}", response.statusCode(), address);
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode documents = root.path("documents");
            if (!documents.isArray() || documents.isEmpty()) {
                return Optional.empty();
            }

            JsonNode first = documents.get(0);
            Double latitude = parseDouble(first.path("y").asText(null));
            Double longitude = parseDouble(first.path("x").asText(null));
            if (latitude == null || longitude == null) {
                return Optional.empty();
            }

            return Optional.of(new Coordinates(latitude, longitude));
        } catch (Exception ex) {
            log.warn("Kakao geocoding exception. address={}", address, ex);
            return Optional.empty();
        }
    }

    private String buildRequestUrl(String address) {
        String baseUrl = normalizeBaseUrl(kakaoLocalSearchBaseUrl);
        String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
        return baseUrl + ADDRESS_SEARCH_PATH + "?query=" + encodedAddress;
    }

    private String normalizeBaseUrl(String value) {
        if (value == null || value.isBlank()) {
            return "https://dapi.kakao.com";
        }
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record Coordinates(Double latitude, Double longitude) {
    }
}
