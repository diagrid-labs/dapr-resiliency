package com.service.controller;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@RestController
public class Controller {
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);
    private static final String DAPR_HTTP_PORT = System.getenv().getOrDefault("DAPR_HTTP_PORT", "3500");

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofMinutes(10))
            .build();

    // Publish messages
    @PostMapping(path = "/checkout", consumes = MediaType.ALL_VALUE)
    public Mono<ResponseEntity> checkout(@RequestBody(required = true) Order order) {
        return Mono.fromSupplier(() -> {

            try {
                int orderId = order.getOrderId();
                JSONObject obj = new JSONObject();
                obj.put("orderId", orderId);

                HttpRequest request = HttpRequest.newBuilder()
                        .POST(HttpRequest.BodyPublishers.ofString(obj.toString()))
                        .uri(URI.create("http://localhost:" + DAPR_HTTP_PORT + "/process"))
                        .header("dapr-app-id", "order-processor")
                        .header("Content-Type", "application/json")
                        .build();

                System.out.println("Sending order: "+ orderId);
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("Response: Status " + response.statusCode() + " Body " + response.body());
                return ResponseEntity.status(response.statusCode()).body(response.body());
            } catch (Exception e) {
                logger.error("Error occurred while publishing order: " + order.getOrderId());
                throw new RuntimeException(e);
            }
        });
    }
}

@Getter
@Setter
@ToString
class Order {
    private int orderId;
}