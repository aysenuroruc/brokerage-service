package model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
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
