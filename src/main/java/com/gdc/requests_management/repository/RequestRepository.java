package com.gdc.requests_management.repository;

import com.gdc.requests_management.model.Request;
import com.gdc.requests_management.model.enums.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RequestRepository extends JpaRepository<Request, UUID> {

    @Query("SELECT r FROM Request r WHERE r.sender.userId = :senderId")
    List<Request> findBySenderId(@Param("senderId") UUID senderId);

    @Query("SELECT r FROM Request r WHERE r.assignedDriver.driverId = :driverId AND r.status IN :statuses")
    List<Request> findByAssignedDriverIdAndStatusIn(@Param("driverId") UUID driverId, @Param("statuses") List<RequestStatus> statuses);

    @Query("SELECT r FROM Request r WHERE r.ride.rideId = :rideId")
    List<Request> findByRideId(@Param("rideId") UUID rideId);

    @Query("SELECT r FROM Request r WHERE r.status = :status")
    List<Request> findByStatus(@Param("status") RequestStatus status);
}