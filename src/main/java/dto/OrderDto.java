package dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderDto {
    private Long id;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotBlank(message = "Asset name is required")
    private String assetName;

    @NotBlank(message = "Order side is required (BUY or SELL)")
    private String orderSide;

    @NotNull(message = "Size is required")
    private Integer size;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    private String status;

    private String createDate;
}
