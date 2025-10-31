package model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import model.OrderSide;
import model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long customerId;
    private String assetName;

    @Enumerated(EnumType.STRING)
    private OrderSide orderSide;

    private BigDecimal size;
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime createDate;

    @Version
    private Long version;
}
