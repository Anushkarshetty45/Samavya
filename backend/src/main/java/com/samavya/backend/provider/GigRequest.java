package com.samavya.backend.provider;

import com.samavya.backend.PricingType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record GigRequest(
        @NotBlank @Size(max = 200) String title,
        String description,
        @NotNull PricingType pricingType,
        @Digits(integer = 10, fraction = 2) BigDecimal price) {
}
