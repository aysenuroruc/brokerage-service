package dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class AssetDto {
    private Long id;

    @NotNull
    private Long customerId;

    @NotBlank
    private String assetName;

    @NotNull
    private BigDecimal size;

    @NotNull
    private BigDecimal usableSize;
}
