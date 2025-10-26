package controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.OrderDto;
import model.OrderSide;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import service.OrderService;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderDto validOrder;

    @BeforeEach
    void setUp() {
        validOrder = new OrderDto();
        validOrder.setCustomerId(1L);
        validOrder.setAssetName("THYAO");
        validOrder.setOrderSide(OrderSide.BUY.name());
        validOrder.setPrice(BigDecimal.valueOf(100));
        validOrder.setSize(BigDecimal.valueOf(5));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void createOrder_ShouldReturnOk_WhenValidOrder() throws Exception {
        when(orderService.createOrder(any(OrderDto.class))).thenReturn(validOrder);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrder)))
                .andExpect(status().isOk());

        verify(orderService, times(1)).createOrder(any(OrderDto.class));
    }

    @Test
    void createOrder_ShouldReturnUnauthorized_WhenNoAuth() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrder)))
                .andExpect(status().isUnauthorized());
    }
}
