package com.vietmart.product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controller mô phỏng trong Product Service để kiểm chứng cân bằng tải.
 * Ghi log chi tiết mỗi khi có request tới để quan sát sự phân phối của LoadBalancer.
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final AtomicInteger requestCounter = new AtomicInteger(0);

    @Value("${server.port}")
    private String serverPort;

    @Value("${spring.application.name:product-service}")
    private String appName;

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProductById(@PathVariable Long id) {
        int count = requestCounter.incrementAndGet();
        log.info(">>> [PRODUCT-SERVICE | Port: {}] Xử lý request thứ {} cho Product ID: {}", serverPort, count, id);

        Map<String, Object> response = new HashMap<>();
        response.put("productId", id);
        response.put("productName", "Sản phẩm mẫu VietMart #" + id);
        response.put("serverPort", serverPort);
        response.put("serviceName", appName);
        response.put("requestIndexOnInstance", count);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }
}
