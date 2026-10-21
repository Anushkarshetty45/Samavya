package com.samavya.backend.provider;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

public record ProviderServiceRequest(
        @NotNull @Positive Long communityId,
        @NotNull @Positive Long categoryId,
        @DecimalMin("0") @Digits(integer = 10, fraction = 2) BigDecimal priceFrom) {
}
