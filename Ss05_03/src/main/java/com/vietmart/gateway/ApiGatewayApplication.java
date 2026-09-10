package com.vietmart.gateway;

import com.vietmart.gateway.config.RandomLoadBalancerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;

/**
 * Ứng dụng trung tâm API Gateway của VietMart.
 * 
 * Áp dụng cấu hình RandomLoadBalancerConfig RIÊNG BIỆT cho 'product-service'.
 * Các service khác (như order-service, voucher-service) vẫn duy trì thuật toán Round Robin mặc định.
 */
@SpringBootApplication
@LoadBalancerClient(name = "product-service", configuration = RandomLoadBalancerConfig.class)
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
