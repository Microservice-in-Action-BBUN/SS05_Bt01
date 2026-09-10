package com.vietmart.search.v1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Ứng dụng giả lập search-service-v1 (Phiên bản cũ ổn định - 80% traffic).
 * Chạy trên Port 8081 và đăng ký Eureka với tên 'search-service-v1'.
 */
@SpringBootApplication
@RestController
@RequestMapping("/api/search")
public class SearchServiceV1Application {

    private static final Logger log = LoggerFactory.getLogger(SearchServiceV1Application.class);

    public static void main(String[] args) {
        // Đặt port mặc định là 8081 nếu không truyền qua command line
        System.setProperty("server.port", System.getProperty("server.port", "8081"));
        System.setProperty("spring.application.name", "search-service-v1");
        SpringApplication.run(SearchServiceV1Application.class, args);
    }

    @GetMapping
    public String searchPlainText(@RequestParam(required = false, defaultValue = "") String keyword) {
        log.info("[SEARCH-V1] Xử lý request tìm kiếm từ khóa: '{}'", keyword);
        return "V1";
    }

    @GetMapping("/detail")
    public Map<String, Object> searchJson(@RequestParam(required = false, defaultValue = "") String keyword) {
        log.info("[SEARCH-V1-DETAIL] Xử lý request tìm kiếm chi tiết: '{}'", keyword);
        Map<String, Object> result = new HashMap<>();
        result.put("version", "V1");
        result.put("algorithm", "Traditional Lucene Full-text Search");
        result.put("trafficRatio", "80%");
        result.put("results", Collections.singletonList("Kết quả tìm kiếm từ V1"));
        return result;
    }
}
