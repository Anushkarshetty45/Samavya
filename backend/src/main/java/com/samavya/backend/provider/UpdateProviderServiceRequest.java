package com.samavya.backend.provider;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateProviderServiceRequest(
        @DecimalMin("0") @Digits(integer = 10, fraction = 2) BigDecimal priceFrom,
        @NotNull Boolean active) {
}
