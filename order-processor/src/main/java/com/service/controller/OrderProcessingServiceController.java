package com.service.controller;

import com.service.model.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.time.Duration;

@RestController
public class OrderProcessingServiceController {
    private long firstRequestTimestamp = 0;

    @PostMapping(path = "/process", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<String> process(@RequestBody Order body) throws InterruptedException {
        int orderId = body.getOrderId();

        switch (orderId) {

            case 200:
            {
                System.out.println("Request Successful (200) ✅");
                return ResponseEntity.status(HttpStatus.OK).body("Success for order: " + body.getOrderId());
            }

            case 10:
            {
                if (Math.random() < 0.1) { // 10% chance
                    System.out.println("Request Slow       (200) ⏳");
                    Thread.sleep(Duration.ofSeconds(10).toMillis());
                } else {
                    System.out.println("Request Successful (200) ✅");
                }
                return ResponseEntity.status(HttpStatus.OK).body("Response for order: " + body.getOrderId());
            }

            case 500:
            {
                System.out.println("Internal Server Error (500) ❌");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failure for order: " + body.getOrderId());
            }

            case 501:
            {
                System.out.println("Feature Not Supported (501) ⛔");
                return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Failure for order: " + body.getOrderId());
            }

            case 503:
            {
                long currentTime = System.currentTimeMillis();
                if (firstRequestTimestamp == 0) {
                    firstRequestTimestamp = currentTime;
                }

                // 10-second overload period
                if (currentTime - firstRequestTimestamp <= 10000) {
                    System.out.println("Service Temporarily Overloaded (503) 🔄");
                    return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Failure for order: " + body.getOrderId());
                }

                System.out.println("Request Successful (200) ✅");
                return ResponseEntity.status(HttpStatus.OK).body("Success for order: " + body.getOrderId());
            }

            case 999: {
                firstRequestTimestamp = 0;
                System.out.println("Server overloaded");
            }

            default: {
                System.out.println("Request Successful (200) ✅");
                return ResponseEntity.status(HttpStatus.OK).body("Success for order: " + body.getOrderId());

            }
        }
    }
}
