package controller;
import com.brokerage.service.BrokerageServiceApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.OrderSide;
import model.OrderStatus;
import model.Role;
import model.entity.Order;
import model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import repository.OrderRepository;
import repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@SpringBootTest(classes = BrokerageServiceApplication.class)
public class OrderIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Mock
    private UserRepository userRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        orderRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String login(String username, String password) throws Exception {
        var body = objectMapper.writeValueAsString(new HashMap<String, String>() {{
            put("username", username);
            put("password", password);
        }});

        var result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    void customer_can_create_order_successfully() throws Exception {
        // Arrange
        User customer = new User(null, "creator", passwordEncoder.encode("1234"), Role.CUSTOMER, 11L);
        userRepository.save(customer);

        String token = login("creator", "1234");

        var orderDto = new HashMap<String, Object>() {{
            put("assetName", "BTC");
            put("orderSide", "BUY");
            put("size", 2.5);
            put("price", 120000);
        }};

        // Act
        mockMvc.perform(post("/api/11/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(orderDto)))
                .andExpect(status().isCreated());

        // Assert
        List<Order> allOrders = orderRepository.findAll();
        assertThat(allOrders).hasSize(1);
        assertThat(allOrders.getFirst().getAssetName()).isEqualTo("BTC");
        assertThat(allOrders.getFirst().getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void customer_can_get_their_own_orders() throws Exception {
        // Arrange
        User customer = new User(null, "owner", passwordEncoder.encode("pw"), Role.CUSTOMER, 21L);
        userRepository.save(customer);

        Order order1 = new Order();
        order1.setCustomerId(21L);
        order1.setAssetName("ETH");
        order1.setOrderSide(OrderSide.SELL);
        order1.setSize(BigDecimal.valueOf(1.2));
        order1.setPrice(BigDecimal.valueOf(3000));
        order1.setStatus(OrderStatus.PENDING);
        order1.setCreateDate(LocalDateTime.now());

        orderRepository.save(order1);

        String token = login("owner", "pw");

        // Act + Assert
        mockMvc.perform(get("/api/21/orders/customer/21")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].assetName").value("ETH"))
                .andExpect(jsonPath("$[0].orderSide").value("SELL"));
    }

    @Test
    void customer_cannot_access_others_orders() throws Exception {
        // Arrange
        User c1 = new User(null, "u1", passwordEncoder.encode("p1"), Role.CUSTOMER, 31L);
        User c2 = new User(null, "u2", passwordEncoder.encode("p2"), Role.CUSTOMER, 32L);
        userRepository.saveAll(List.of(c1, c2));

        Order order = new Order();
        order.setCustomerId(32L);
        order.setAssetName("XRP");
        order.setOrderSide(OrderSide.BUY);
        order.setSize(BigDecimal.ONE);
        order.setPrice(BigDecimal.TEN);
        order.setStatus(OrderStatus.PENDING);
        order.setCreateDate(LocalDateTime.now());
        orderRepository.save(order);

        String token = login("u1", "p1");

        // Act + Assert
        mockMvc.perform(get("/api/31/orders/customer/32")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void admin_can_get_all_orders() throws Exception {
        // Arrange
        User admin = new User(null, "adminx", passwordEncoder.encode("root"), Role.ADMIN, null);
        User c1 = new User(null, "cc1", passwordEncoder.encode("pp1"), Role.CUSTOMER, 41L);
        userRepository.saveAll(List.of(admin, c1));

        Order order = new Order();
        order.setCustomerId(41L);
        order.setAssetName("SOL");
        order.setOrderSide(OrderSide.BUY);
        order.setSize(BigDecimal.valueOf(5));
        order.setPrice(BigDecimal.valueOf(250));
        order.setStatus(OrderStatus.PENDING);
        order.setCreateDate(LocalDateTime.now());
        orderRepository.save(order);

        String token = login("adminx", "root");

        // Act + Assert
        mockMvc.perform(get("/api/41/orders/customer/41")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].assetName").value("SOL"));
    }


    @Test
    void customer_can_cancel_own_order() throws Exception {
        User customer = new User(null, "cust1", passwordEncoder.encode("pwd"), Role.CUSTOMER, 1L);
        userRepository.save(customer);

        Order order = new Order();
        order.setCustomerId(1L);
        order.setAssetName("TEST");
        order.setOrderSide(OrderSide.BUY);
        order.setSize(BigDecimal.ONE);
        order.setPrice(BigDecimal.TEN);
        order.setStatus(OrderStatus.PENDING);
        order.setCreateDate(LocalDateTime.now());
        order = orderRepository.save(order);

        String token = login("cust1", "pwd");

        mockMvc.perform(delete("/api/1/orders/{orderId}", order.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(orderRepository.findById(order.getId())).isEmpty();
    }

    @Test
    void customer_cannot_cancel_others_order() throws Exception {
        User c1 = new User(null, "c1", passwordEncoder.encode("p1"), Role.CUSTOMER, 1L);
        User c2 = new User(null, "c2", passwordEncoder.encode("p2"), Role.CUSTOMER, 2L);
        userRepository.saveAll(List.of(c1, c2));

        Order order = new Order();
        order.setCustomerId(2L);
        order.setAssetName("ASSET-X");
        order.setOrderSide(OrderSide.SELL);
        order.setSize(BigDecimal.ONE);
        order.setPrice(BigDecimal.TEN);
        order.setStatus(OrderStatus.PENDING);
        order.setCreateDate(LocalDateTime.now());
        order = orderRepository.save(order);

        String token = login("c1", "p1");

        mockMvc.perform(delete("/api/1/orders/{orderId}", order.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        assertThat(orderRepository.findById(order.getId())).isPresent();
    }

    @Test
    void admin_can_cancel_any_order() throws Exception {
        User admin = new User(null, "admin", passwordEncoder.encode("adminpwd"), Role.ADMIN, null);
        User customer = new User(null, "cust", passwordEncoder.encode("custpwd"), Role.CUSTOMER, 5L);
        userRepository.saveAll(List.of(admin, customer));

        Order order = new Order();
        order.setCustomerId(5L);
        order.setAssetName("Y");
        order.setOrderSide(OrderSide.BUY);
        order.setSize(BigDecimal.ONE);
        order.setPrice(BigDecimal.TEN);
        order.setStatus(OrderStatus.PENDING);
        order.setCreateDate(LocalDateTime.now());
        order = orderRepository.save(order);

        String token = login("admin", "adminpwd");

        mockMvc.perform(delete("/api/5/orders/{orderId}", order.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertThat(orderRepository.findById(order.getId())).isEmpty();
    }
}


