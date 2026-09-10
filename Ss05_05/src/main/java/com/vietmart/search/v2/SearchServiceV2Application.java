package com.vietmart.search.v2;

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
 * Ứng dụng giả lập search-service-v2 (Phiên bản mới thử nghiệm - 20% traffic).
 * Chạy trên Port 8082 và đăng ký Eureka với tên 'search-service-v2'.
 */
@SpringBootApplication
@RestController
@RequestMapping("/api/search")
public class SearchServiceV2Application {

    private static final Logger log = LoggerFactory.getLogger(SearchServiceV2Application.class);

    public static void main(String[] args) {
        // Đặt port mặc định là 8082 nếu không truyền qua command line
        System.setProperty("server.port", System.getProperty("server.port", "8082"));
        System.setProperty("spring.application.name", "search-service-v2");
        SpringApplication.run(SearchServiceV2Application.class, args);
    }

    @GetMapping
    public String searchPlainText(@RequestParam(required = false, defaultValue = "") String keyword) {
        log.info("[SEARCH-V2] Xử lý request tìm kiếm từ khóa tối ưu: '{}'", keyword);
        return "V2";
    }

    @GetMapping("/detail")
    public Map<String, Object> searchJson(@RequestParam(required = false, defaultValue = "") String keyword) {
        log.info("[SEARCH-V2-DETAIL] Xử lý request tìm kiếm chi tiết bằng AI: '{}'", keyword);
        Map<String, Object> result = new HashMap<>();
        result.put("version", "V2");
        result.put("algorithm", "AI Vector Search + Semantic Re-ranking");
        result.put("trafficRatio", "20%");
        result.put("results", Collections.singletonList("Kết quả tìm kiếm AI từ V2"));
        return result;
    }
}
