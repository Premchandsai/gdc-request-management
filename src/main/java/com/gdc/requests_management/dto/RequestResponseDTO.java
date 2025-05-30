package com.gdc.requests_management.dto;

import com.gdc.requests_management.model.enums.RequestStatus;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestResponseDTO {
    private UUID id;
    private UUID senderId;
    private UUID rideId;
    private String goodsDescription;
    private Double weight;
    private Double volume;
    private RequestStatus status;
    private UUID assignedDriverId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}