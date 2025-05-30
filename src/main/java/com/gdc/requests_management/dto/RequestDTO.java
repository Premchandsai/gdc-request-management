package com.gdc.requests_management.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestDTO {
    @NotNull(message = "Sender ID is required")
    private UUID senderId;

    @NotNull(message = "Ride ID is required")
    private UUID rideId;

    @NotBlank(message = "Goods description is required")
    private String goodsDescription;

    @NotNull(message = "Weight is required")
    @Positive(message = "Weight must be positive")
    private Double weight;

    @NotNull(message = "Volume is required")
    @Positive(message = "Volume must be positive")
    private Double volume;
}