package model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Asset {
    @Id
    @GeneratedValue
    private Long id;
    private Long customerId;
    private String assetName; // ASELS, TRY ..
    private Double size;
    private Double usableSize;
}
