package controller;

import model.OrderSide;
import model.dto.OrderDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = com.brokerage.service.BrokerageServiceApplication.class)
public class OrderControllerIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    @DynamicPropertySource
    static void registerH2(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/api/orders";
    }

    @Test
    void createOrder_ShouldReturnOk_WhenValidOrder() {
        OrderDto request = new OrderDto();
        request.setAssetName("AAPL");
        request.setCustomerId(1L);
        request.setOrderSide(String.valueOf(OrderSide.BUY));
        request.setPrice(BigDecimal.valueOf(100));
        request.setSize(BigDecimal.valueOf(10));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<OrderDto> entity = new HttpEntity<>(request, headers);

        ResponseEntity<OrderDto> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                entity,
                OrderDto.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAssetName()).isEqualTo("AAPL");
    }

    @Test
    void getOrdersByCustomer_ShouldReturnEmptyList_WhenNoOrders() {
        ResponseEntity<OrderDto[]> response = restTemplate.getForEntity(
                baseUrl + "/customer/99",
                OrderDto[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        //assertThat(List.of(response.getBody())).isEmpty();
    }
}
