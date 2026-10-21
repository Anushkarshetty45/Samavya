package com.samavya.backend.requirements;

import com.samavya.backend.RequirementType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record CreateRequirementRequest(
        @NotNull RequirementType type,
        Long serviceCategoryId,
        @NotBlank @Size(max = 200) String title,
        String description,
        @DecimalMin(value = "0", inclusive = false)
        @Digits(integer = 10, fraction = 2)
        BigDecimal quantity,
        @Size(max = 40) String quantityUnit,
        @DecimalMin("0")
        @Digits(integer = 10, fraction = 2)
        BigDecimal budget,
        OffsetDateTime neededBy) {
}
