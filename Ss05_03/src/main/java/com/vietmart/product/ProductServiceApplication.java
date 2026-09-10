package com.vietmart.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ứng dụng độc lập Product Service của VietMart.
 * Cho phép khởi chạy nhiều instance trên các port khác nhau (ví dụ 8081 và 8082).
 */
@SpringBootApplication
public class ProductServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}
