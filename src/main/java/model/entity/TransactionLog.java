package model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "transaction_logs")
public class TransactionLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long orderId;
    private String assetName;
    private String orderSide;
    private BigDecimal size;
    private BigDecimal price;
    private String result; // SUCCESS / FAILED / SKIPPED
    private String message;
    private LocalDateTime timestamp;
}
