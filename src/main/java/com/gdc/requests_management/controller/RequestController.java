package com.gdc.requests_management.controller;

import com.gdc.requests_management.dto.RequestDTO;
import com.gdc.requests_management.dto.RequestResponseDTO;
import com.gdc.requests_management.model.Request;
import com.gdc.requests_management.service.RequestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/requests")
public class RequestController {
    @Autowired
    private RequestService requestService;

    @PostMapping
    public ResponseEntity<RequestResponseDTO> createRequest(@Valid @RequestBody RequestDTO requestDTO) {
        Request createdRequest = requestService.createRequest(requestDTO);
        RequestResponseDTO response = convertToResponseDTO(createdRequest);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RequestResponseDTO>> getRequestsByUser(@PathVariable UUID userId) {
        List<Request> requests = requestService.getRequestsByUser(userId);
        List<RequestResponseDTO> response = requests.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<RequestResponseDTO> getRequestById(@PathVariable UUID requestId) {
        Request request = requestService.getRequestById(requestId);
        RequestResponseDTO response = convertToResponseDTO(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/driver/{driverId}/active")
    public ResponseEntity<List<RequestResponseDTO>> getActiveRequestsForDriver(@PathVariable UUID driverId) {
        List<Request> requests = requestService.getActiveRequestsForDriver(driverId);
        List<RequestResponseDTO> response = requests.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{requestId}/status")
    public ResponseEntity<RequestResponseDTO> updateRequestStatus(@PathVariable UUID requestId,
                                                                  @RequestParam String status,
                                                                  @AuthenticationPrincipal String userId) {
        Request updatedRequest = requestService.updateRequestStatus(requestId, status, "DRIVER");
        RequestResponseDTO response = convertToResponseDTO(updatedRequest);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{requestId}/assign")
    public ResponseEntity<RequestResponseDTO> assignDriver(@PathVariable UUID requestId,
                                                           @RequestParam UUID driverId,
                                                           @AuthenticationPrincipal String userId) {
        Request updatedRequest = requestService.assignDriver(requestId, driverId, "ADMIN");
        RequestResponseDTO response = convertToResponseDTO(updatedRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{requestId}/cancel")
    public ResponseEntity<Void> cancelRequest(@PathVariable UUID requestId,
                                              @AuthenticationPrincipal String userId) {
        requestService.cancelRequest(requestId, UUID.fromString(userId));
        return ResponseEntity.noContent().build();
    }

    private RequestResponseDTO convertToResponseDTO(Request request) {
        RequestResponseDTO dto = new RequestResponseDTO();
        dto.setId(request.getId());
        dto.setSenderId(request.getSenderId());
        dto.setRideId(request.getRideId());
        dto.setGoodsDescription(request.getGoodsDescription());
        dto.setWeight(request.getWeight());
        dto.setVolume(request.getVolume());
        dto.setStatus(request.getStatus());
        dto.setAssignedDriverId(request.getAssignedDriverId());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUpdatedAt(request.getUpdatedAt());
        return dto;
    }
}