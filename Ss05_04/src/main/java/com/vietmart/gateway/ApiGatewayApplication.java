package com.vietmart.gateway;

import com.vietmart.loadbalancer.strategies.order.OrderZonePreferenceConfig;
import com.vietmart.loadbalancer.strategies.product.ProductRandomConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;

/**
 * Ứng dụng Gateway chính của VietMart.
 * 
 * Sử dụng @LoadBalancerClients để gán từng chiến lược cấu hình riêng biệt cho từng service.
 * Do các lớp cấu hình nằm trong package 'com.vietmart.loadbalancer.strategies.*',
 * chúng hoàn toàn nằm ngoài phạm vi quét mặc định của @SpringBootApplication (com.vietmart.gateway.*),
 * tránh 100% nguy cơ ô nhiễm Root Context và xung đột cấu hình giữa các service.
 */
@SpringBootApplication
@LoadBalancerClients({
        @LoadBalancerClient(name = "order-service", configuration = OrderZonePreferenceConfig.class),
        @LoadBalancerClient(name = "product-service", configuration = ProductRandomConfig.class)
})
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
