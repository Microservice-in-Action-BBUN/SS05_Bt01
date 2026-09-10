package com.vietmart.gateway.config;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.RandomLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Lớp cấu hình tùy biến thuật toán cân bằng tải cho Spring Cloud LoadBalancer.
 * 
 * Thay thế thuật toán mặc định (RoundRobinLoadBalancer) bằng RandomLoadBalancer
 * dành riêng cho service được chỉ định qua @LoadBalancerClient.
 */
@Configuration
public class RandomLoadBalancerConfig {

    /**
     * Cung cấp Bean ReactorLoadBalancer<ServiceInstance> triển khai thuật toán ngẫu nhiên (Random).
     *
     * @param environment Môi trường Spring để lấy tên service mục tiêu từ LoadBalancerClientFactory.PROPERTY_NAME
     * @param loadBalancerClientFactory Factory quản lý các ServiceInstanceListSupplier cho từng service
     * @return ReactorLoadBalancer<ServiceInstance> triển khai RandomLoadBalancer
     */
    @Bean
    public ReactorLoadBalancer<ServiceInstance> randomLoadBalancer(
            Environment environment,
            LoadBalancerClientFactory loadBalancerClientFactory) {

        // Lấy serviceId từ context của LoadBalancer (ví dụ: "product-service")
        String serviceId = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);

        // Khởi tạo RandomLoadBalancer với lazy provider cung cấp danh sách instance của serviceId
        return new RandomLoadBalancer(
                loadBalancerClientFactory.getLazyProvider(serviceId, ServiceInstanceListSupplier.class),
                serviceId
        );
    }
}
