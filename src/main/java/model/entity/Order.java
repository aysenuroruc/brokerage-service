package model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import model.OrderSide;
import model.OrderStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long customerId;
    private String assetName;
    @Enumerated(EnumType.STRING)
    private OrderSide orderSide;
    private Double size;
    private Double price;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private LocalDateTime createDate;
}
