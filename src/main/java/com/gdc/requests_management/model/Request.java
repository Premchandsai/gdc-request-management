package com.gdc.requests_management.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gdc.requests_management.model.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "requests")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id", nullable = false, foreignKey = @ForeignKey(name = "fk_request_sender"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private UserInfo sender;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ride_id", nullable = false, foreignKey = @ForeignKey(name = "fk_request_ride"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Ride ride;

    @Column(name = "goods_description", nullable = false, length = 500)
    private String goodsDescription;

    @Column(nullable = false)
    private Double weight;

    @Column(nullable = false)
    private Double volume;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_driver_id", foreignKey = @ForeignKey(name = "fk_request_driver"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Driver assignedDriver;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods for backward compatibility
    public UUID getSenderId() {
        return sender != null ? sender.getUserId() : null;
    }

    public void setSenderId(UUID senderId) {
        if (this.sender == null) {
            this.sender = new UserInfo();
        }
        this.sender.setUserId(senderId);
    }

    public UUID getRideId() {
        return ride != null ? ride.getRideId() : null;
    }

    public void setRideId(UUID rideId) {
        if (this.ride == null) {
            this.ride = new Ride();
        }
        this.ride.setRideId(rideId);
    }

    public UUID getAssignedDriverId() {
        return assignedDriver != null ? assignedDriver.getDriverId() : null;
    }

    public void setAssignedDriverId(UUID driverId) {
        if (driverId != null) {
            if (this.assignedDriver == null) {
                this.assignedDriver = new Driver();
            }
            this.assignedDriver.setDriverId(driverId);
        } else {
            this.assignedDriver = null;
        }
    }
}