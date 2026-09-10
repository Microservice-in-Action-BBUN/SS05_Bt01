package com.vietmart.loadbalancer.strategies.order;

import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Cấu hình Zone Preference chỉ áp dụng riêng cho order-service.
 * 
 * LƯU Ý QUAN TRỌNG:
 * Package này (com.vietmart.loadbalancer.strategies.order) nằm NGOÀI phạm vi quét của
 * @ComponentScan từ ApiGatewayApplication (com.vietmart.gateway.*),
 * đảm bảo Bean này KHÔNG bị rò rỉ vào Root Application Context.
 */
public class OrderZonePreferenceConfig {

    @Bean
    public ServiceInstanceListSupplier discoveryClientServiceInstanceListSupplier(
            ConfigurableApplicationContext context) {
        return ServiceInstanceListSupplier.builder()
                .withDiscoveryClient()
                .withZonePreference()
                .withHealthChecks()
                .build(context);
    }
}
