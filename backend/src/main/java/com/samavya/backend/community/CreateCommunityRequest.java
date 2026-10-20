package com.samavya.backend.community;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCommunityRequest(
        @NotBlank @Size(max = 150) String name,
        String description,
        String address) {
}
