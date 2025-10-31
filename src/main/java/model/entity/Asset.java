package model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "assets")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long customerId;
    private String assetName;
    private BigDecimal size;
    private BigDecimal usableSize;

    @Version
    private Long version;
}
