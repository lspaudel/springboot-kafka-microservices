package net.kafkajava.base_domains.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    private String orderId;

    @NotBlank(message = "Order name must not be blank")
    private String name;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int qty;

    @Positive(message = "Price must be a positive value")
    private double price;

}
