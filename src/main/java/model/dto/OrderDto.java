package model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderDto {
    private Long id;

    @NotNull(message = "Customer ID cannot be null")
    private Long customerId;

    @NotBlank(message = "Asset name cannot be blank")
    private String assetName;

    @NotBlank(message = "Order side cannot be blank (BUY or SELL)")
    private String orderSide;

    @NotNull(message = "Order size cannot be null")
    @DecimalMin(value = "0.01", message = "Order size must be greater than 0")
    private BigDecimal size;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    private String status;

    private String createDate;
}
