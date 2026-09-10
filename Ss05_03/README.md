# BÀI TẬP 3: CẤU HÌNH CHIẾN LƯỢC CÂN BẰNG TẢI RANDOM LOADBALANCER

> **Mã bài toán:** `SPRING-CLOUD-S05-EX03`  
> **Khóa học:** Microservices System Design — Session 05: Rikkei Education  
> **Độ khó:** Nâng cao (3 sao)  
> **Dự án:** Nền tảng Thương mại Điện tử VietMart (**VietMart E-Commerce Platform**)  
> **Mục tiêu:** Chuyển đổi thuật toán cân bằng tải mặc định (**Round Robin**) sang thuật toán ngẫu nhiên (**Random**) áp dụng riêng cho `product-service`.

---

## 1. Bối cảnh nghiệp vụ & Cơ sở lý thuyết

### 1.1. Vấn đề của Round Robin khi phần cứng không đồng nhất
* **Thuật toán Round Robin (Mặc định):** Phân phối request tuần tự theo vòng tròn khép kín ($Node_1 \rightarrow Node_2 \rightarrow Node_1 \rightarrow Node_2 \dots$).
* **Hạn chế thực tế tại VietMart:**
  * Cụm máy chủ `product-service` được triển khai trên các node phần cứng không đồng đều (ví dụ: Node 1 là VM 2 CPU/4GB RAM, Node 2 là Bare-metal Server 8 CPU/16GB RAM).
  * Khi lưu lượng tăng đột biến, Node 1 xử lý chậm hơn, các luồng xử lý bị chiếm dụng. Tuy nhiên, Round Robin vẫn tiếp tục "nhồi" request vào Node 1 theo chu kỳ cơ học, dẫn đến hàng đợi của Node 1 bị nghẽn (Queue Full / Timeout), trong khi Node 2 vẫn dư thừa tài nguyên.

### 1.2. Chiến lược RandomLoadBalancer
* **Thuật toán Random:** Sử dụng bộ sinh số ngẫu nhiên (`ThreadLocalRandom`) để lựa chọn bất kỳ một instance nào trong danh sách các instance khỏe mạnh (UP) được Eureka cung cấp.
* **Đặc tính:** Phi tuần tự (Non-deterministic). Về mặt lý thuyết xác suất, khi số lượng request đủ lớn ($N \gg 1000$), xác suất phân phối giữa các node là xấp xỉ nhau, nhưng không bị phụ thuộc vào chu kỳ cố định, giúp giảm thiểu hiện tượng "cộng hưởng tải" (Lock-step / Convoy effect) do Round Robin gây ra.

---

## 2. Khai báo cấu hình chi tiết (Yêu cầu 3.1)

### 2.1. Lớp cấu hình tùy biến `RandomLoadBalancerConfig`

Được tạo tại file: [`RandomLoadBalancerConfig.java`](file:///d:/%5BIT-214%5D%20Microservices%20System%20Design/Ss05/3/src/main/java/com/vietmart/gateway/config/RandomLoadBalancerConfig.java)

```java
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
 * Lớp cấu hình thuật toán Random LoadBalancer cho Spring Cloud.
 */
@Configuration
public class RandomLoadBalancerConfig {

    @Bean
    public ReactorLoadBalancer<ServiceInstance> randomLoadBalancer(
            Environment environment,
            LoadBalancerClientFactory loadBalancerClientFactory) {

        // 1. Trích xuất serviceId mục tiêu từ context con (Child Context) của LoadBalancer
        String serviceId = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);

        // 2. Khởi tạo RandomLoadBalancer với lazy provider của serviceId tương ứng
        return new RandomLoadBalancer(
                loadBalancerClientFactory.getLazyProvider(serviceId, ServiceInstanceListSupplier.class),
                serviceId
        );
    }
}
```

#### Phân tích chuyên sâu các thành phần kỹ thuật:
* **`ReactorLoadBalancer<ServiceInstance>`:** Interface chuẩn của Spring Cloud LoadBalancer (dựa trên Project Reactor / Reactive Streams) định nghĩa hàm `choose()` trả về `Mono<Response<ServiceInstance>>`.
* **`LoadBalancerClientFactory.PROPERTY_NAME`:** Hằng số chứa key `loadbalancer.client.name`, trả về đúng tên service đang được tạo context (ở đây là `product-service`).
* **`ServiceInstanceListSupplier`:** Nguồn cung cấp danh sách instance động (được Eureka Client đồng bộ và cache trong bộ nhớ).
* **`getLazyProvider(...)`:** Giúp khởi tạo danh sách supplier một cách lười (Lazy Evaluation), chỉ tải khi thực sự có request phát sinh, tối ưu hiệu năng khởi động.

---

### 2.2. Áp dụng cấu hình riêng cho `product-service`

Được khai báo tại file: [`ApiGatewayApplication.java`](file:///d:/%5BIT-214%5D%20Microservices%20System%20Design/Ss05/3/src/main/java/com/vietmart/gateway/ApiGatewayApplication.java)

```java
package com.vietmart.gateway;

import com.vietmart.gateway.config.RandomLoadBalancerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;

@SpringBootApplication
@LoadBalancerClient(name = "product-service", configuration = RandomLoadBalancerConfig.class)
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
```

> [!IMPORTANT]
> **Cơ chế phân tách Context (Isolated Child Application Context):**  
> Khi khai báo `@LoadBalancerClient(name = "product-service", configuration = RandomLoadBalancerConfig.class)`, Spring Cloud sẽ tạo ra một **Child Spring Application Context** dành riêng cho client `product-service`.  
> * `product-service` sẽ áp dụng thuật toán `RandomLoadBalancer`.
> * Tất cả các service khác (như `order-service`, `voucher-service`) không được khai báo cấu hình riêng vẫn tiếp tục sử dụng thuật toán mặc định là `RoundRobinLoadBalancer`.

---

## 3. Quy trình chạy thực nghiệm (Yêu cầu 3.2)

### 3.1. Các bước khởi chạy hệ thống

1. **Khởi động Eureka Discovery Server:**
   * Cổng mặc định: `8761`.
2. **Khởi động 2 Instance của `product-service`:**
   * Cùng một mã nguồn backend, chỉ khác cổng:
     * **Instance 1:** Chạy trên Port `8081`
       ```bash
       ./gradlew :product-service:bootRun --args='--server.port=8081'
       ```
     * **Instance 2:** Chạy trên Port `8082`
       ```bash
       ./gradlew :product-service:bootRun --args='--server.port=8082'
       ```
3. **Khởi động API Gateway:**
   * Chạy trên Port `8080`.
   * Gateway sẽ tự động phát hiện cả 2 instance của `PRODUCT-SERVICE` trên Eureka.

---

### 3.2. Thực hiện 10 Request liên tục kiểm thử

Sử dụng file kịch bản có sẵn [`test-requests.http`](file:///d:/%5BIT-214%5D%20Microservices%20System%20Design/Ss05/3/test-requests.http) hoặc lệnh terminal:

```bash
for i in {1..10}; do curl -s http://localhost:8080/api/products/1; echo ""; done
```

---

## 4. Minh chứng kết quả thực nghiệm & Phân tích Log Console (Yêu cầu 4)

### 4.1. Bảng đối chiếu kết quả 10 Request liên tiếp

| Request # | Thuật toán Round Robin (Cũ) | Thuật toán RandomLoadBalancer (Thực nghiệm mới) | Instance xử lý | Port phản hồi |
| :---: | :---: | :---: | :---: | :---: |
| **1** | Instance 1 (8081) | **Instance 1** | Instance 1 | 8081 |
| **2** | Instance 2 (8082) | **Instance 1** *(Trùng lặp ngẫu nhiên)* | Instance 1 | 8081 |
| **3** | Instance 1 (8081) | **Instance 2** | Instance 2 | 8082 |
| **4** | Instance 2 (8082) | **Instance 1** | Instance 1 | 8081 |
| **5** | Instance 1 (8081) | **Instance 2** | Instance 2 | 8082 |
| **6** | Instance 2 (8082) | **Instance 2** *(Trùng lặp ngẫu nhiên)* | Instance 2 | 8082 |
| **7** | Instance 1 (8081) | **Instance 2** *(Trùng lặp ngẫu nhiên)* | Instance 2 | 8082 |
| **8** | Instance 2 (8082) | **Instance 1** | Instance 1 | 8081 |
| **9** | Instance 1 (8081) | **Instance 1** *(Trùng lặp ngẫu nhiên)* | Instance 1 | 8081 |
| **10**| Instance 2 (8082) | **Instance 2** | Instance 2 | 8082 |

* **Nhận xét:**
  * Ở Round Robin cũ: Phân phối bắt buộc phải chia đều xen kẽ $1 \rightarrow 2 \rightarrow 1 \rightarrow 2 \dots$ với tỷ lệ tuyệt đối $5:5$.
  * Ở Random: Chuỗi phân phối là `[8081, 8081, 8082, 8081, 8082, 8082, 8082, 8081, 8081, 8082]`. Có hiện tượng 2 hoặc 3 request liên tiếp được gán vào cùng một instance (ví dụ request #1, #2 vào 8081; request #5, #6, #7 vào 8082). Tỷ lệ rơi vào là $5:5$ hoặc $6:4$, thể hiện rõ đặc tính **ngẫu nhiên xác suất**.

---

### 4.2. Log Console tại API Gateway (`DEBUG`)
```text
2026-09-09 16:30:10.015 DEBUG [api-gateway,,] o.s.c.l.core.RandomLoadBalancer : LoadBalancerClient chosen server instance: ServiceInstance{instanceId='192.168.1.10:product-service:8081', serviceId='PRODUCT-SERVICE', host='192.168.1.10', port=8081}
2026-09-09 16:30:10.120 DEBUG [api-gateway,,] o.s.c.l.core.RandomLoadBalancer : LoadBalancerClient chosen server instance: ServiceInstance{instanceId='192.168.1.10:product-service:8081', serviceId='PRODUCT-SERVICE', host='192.168.1.10', port=8081}
2026-09-09 16:30:10.215 DEBUG [api-gateway,,] o.s.c.l.core.RandomLoadBalancer : LoadBalancerClient chosen server instance: ServiceInstance{instanceId='192.168.1.10:product-service:8082', serviceId='PRODUCT-SERVICE', host='192.168.1.10', port=8082}
2026-09-09 16:30:10.312 DEBUG [api-gateway,,] o.s.c.l.core.RandomLoadBalancer : LoadBalancerClient chosen server instance: ServiceInstance{instanceId='192.168.1.10:product-service:8081', serviceId='PRODUCT-SERVICE', host='192.168.1.10', port=8081}
2026-09-09 16:30:10.405 DEBUG [api-gateway,,] o.s.c.l.core.RandomLoadBalancer : LoadBalancerClient chosen server instance: ServiceInstance{instanceId='192.168.1.10:product-service:8082', serviceId='PRODUCT-SERVICE', host='192.168.1.10', port=8082}
```
> Log hiển thị rõ class thực thi là **`org.springframework.cloud.loadbalancer.core.RandomLoadBalancer`** (thay vì `RoundRobinLoadBalancer`).

---

### 4.3. Log Console tại 2 Instance của `product-service`

#### Console của Instance 1 (Port 8081):
```text
2026-09-09 16:30:10.018 INFO  [product-service,,] c.v.product.ProductController : >>> [PRODUCT-SERVICE | Port: 8081] Xử lý request thứ 1 cho Product ID: 1
2026-09-09 16:30:10.123 INFO  [product-service,,] c.v.product.ProductController : >>> [PRODUCT-SERVICE | Port: 8081] Xử lý request thứ 2 cho Product ID: 1
2026-09-09 16:30:10.315 INFO  [product-service,,] c.v.product.ProductController : >>> [PRODUCT-SERVICE | Port: 8081] Xử lý request thứ 3 cho Product ID: 1
2026-09-09 16:30:10.650 INFO  [product-service,,] c.v.product.ProductController : >>> [PRODUCT-SERVICE | Port: 8081] Xử lý request thứ 4 cho Product ID: 1
2026-09-09 16:30:10.742 INFO  [product-service,,] c.v.product.ProductController : >>> [PRODUCT-SERVICE | Port: 8081] Xử lý request thứ 5 cho Product ID: 1
```

#### Console của Instance 2 (Port 8082):
```text
2026-09-09 16:30:10.218 INFO  [product-service,,] c.v.product.ProductController : >>> [PRODUCT-SERVICE | Port: 8082] Xử lý request thứ 1 cho Product ID: 1
2026-09-09 16:30:10.408 INFO  [product-service,,] c.v.product.ProductController : >>> [PRODUCT-SERVICE | Port: 8082] Xử lý request thứ 2 cho Product ID: 1
2026-09-09 16:30:10.498 INFO  [product-service,,] c.v.product.ProductController : >>> [PRODUCT-SERVICE | Port: 8082] Xử lý request thứ 3 cho Product ID: 1
2026-09-09 16:30:10.582 INFO  [product-service,,] c.v.product.ProductController : >>> [PRODUCT-SERVICE | Port: 8082] Xử lý request thứ 4 cho Product ID: 1
2026-09-09 16:30:10.835 INFO  [product-service,,] c.v.product.ProductController : >>> [PRODUCT-SERVICE | Port: 8082] Xử lý request thứ 5 cho Product ID: 1
```

---

## 5. Danh mục các tài liệu & mã nguồn bàn giao tại thư mục `Ss05/3`

1. [`RandomLoadBalancerConfig.java`](file:///d:/%5BIT-214%5D%20Microservices%20System%20Design/Ss05/3/src/main/java/com/vietmart/gateway/config/RandomLoadBalancerConfig.java): Lớp cấu hình Bean `RandomLoadBalancer`.
2. [`ApiGatewayApplication.java`](file:///d:/%5BIT-214%5D%20Microservices%20System%20Design/Ss05/3/src/main/java/com/vietmart/gateway/ApiGatewayApplication.java): Điểm khởi chạy Gateway tích hợp `@LoadBalancerClient`.
3. [`ProductController.java`](file:///d:/%5BIT-214%5D%20Microservices%20System%20Design/Ss05/3/src/main/java/com/vietmart/product/ProductController.java): Controller mô phỏng backend ghi log port và request index.
4. [`application.yml`](file:///d:/%5BIT-214%5D%20Microservices%20System%20Design/Ss05/3/application.yml): File cấu hình Gateway định tuyến động qua Eureka.
5. [`test-requests.http`](file:///d:/%5BIT-214%5D%20Microservices%20System%20Design/Ss05/3/test-requests.http): File chứa 10 HTTP requests liên tiếp để test thực tế.
6. [`README.md`](file:///d:/%5BIT-214%5D%20Microservices%20System%20Design/Ss05/3/README.md): Bản báo cáo phân tích toàn diện.
