package com.forerp.erp.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TossPaymentsClient {

    private final ObjectMapper objectMapper;

    @Value("${toss.api.base-url:https://api.tosspayments.com}")
    private String baseUrl;

    @Value("${toss.api.secret-key:}")
    private String secretKey;

    @Value("${toss.api.version:2022-11-16}")
    private String apiVersion;

    private RestClient buildClient() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("TOSS_SECRET_KEY가 설정되어 있지 않습니다.");
        }

        String basicToken = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basicToken)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("TossPayments-Version", apiVersion)
                .build();
    }

    public JsonNode confirm(String paymentKey, String merchantOrderId, BigDecimal amount) {
        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", paymentKey);
        body.put("orderId", merchantOrderId);
        body.put("amount", amount);

        return execute(() -> buildClient().post()
                .uri("/v1/payments/confirm")
                .body(body)
                .retrieve()
                .body(String.class));
    }

    public JsonNode getByPaymentKey(String paymentKey) {
        return execute(() -> buildClient().get()
                .uri("/v1/payments/{paymentKey}", paymentKey)
                .retrieve()
                .body(String.class));
    }

    public JsonNode cancel(String paymentKey, String reason, BigDecimal cancelAmount) {
        Map<String, Object> body = new HashMap<>();
        body.put("cancelReason", reason);
        if (cancelAmount != null) {
            body.put("cancelAmount", cancelAmount);
        }

        return execute(() -> buildClient().post()
                .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                .body(body)
                .retrieve()
                .body(String.class));
    }

    private JsonNode execute(HttpCall call) {
        try {
            String responseBody = call.call();
            return objectMapper.readTree(responseBody);
        } catch (RestClientResponseException ex) {
            throw new TossPaymentsException("토스 결제 API 호출에 실패했습니다.", ex.getStatusCode().value(), ex.getResponseBodyAsString());
        } catch (Exception ex) {
            throw new IllegalStateException("토스 결제 API 응답 파싱에 실패했습니다.", ex);
        }
    }

    @FunctionalInterface
    private interface HttpCall {
        String call();
    }
}
