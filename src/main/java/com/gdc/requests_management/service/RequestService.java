package com.gdc.requests_management.service;

import com.gdc.requests_management.model.Driver;
import com.gdc.requests_management.dto.RequestDTO;
import com.gdc.requests_management.model.Request;
import com.gdc.requests_management.model.Ride;
import com.gdc.requests_management.model.UserInfo;
import com.gdc.requests_management.model.enums.RequestStatus;
import com.gdc.requests_management.repository.RequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
@Transactional
public class RequestService {
    private static final Logger logger = Logger.getLogger(RequestService.class.getName());

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired(required = false)
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${service.user:#{null}}")
    private String userServiceUrl;

    @Value("${service.ride:#{null}}")
    private String rideServiceUrl;

    @Value("${service.driver:#{null}}")
    private String driverServiceUrl;

    @Value("${app.validation.skip-external:true}")
    private boolean skipExternalValidation;

    private static final String REQUEST_CACHE_KEY = "request:";

    public Request createRequest(RequestDTO requestDTO) {
        if (!skipExternalValidation) {
            validateSender(requestDTO.getSenderId());
            validateRide(requestDTO.getRideId());
        } else {
            logger.info("Skipping external validation for testing");
        }

        Request request = new Request();

        // Create and set sender
        UserInfo sender = new UserInfo();
        sender.setUserId(requestDTO.getSenderId());
        request.setSender(sender);

        // Create and set ride
        Ride ride = new Ride();
        ride.setRideId(requestDTO.getRideId());
        request.setRide(ride);

        request.setGoodsDescription(requestDTO.getGoodsDescription());
        request.setWeight(requestDTO.getWeight());
        request.setVolume(requestDTO.getVolume());
        request.setStatus(RequestStatus.PENDING);

        try {
            Request savedRequest = requestRepository.save(request);

            // Cache the request
            if (redisTemplate != null) {
                try {
                    redisTemplate.opsForValue().set(REQUEST_CACHE_KEY + savedRequest.getId(), savedRequest, 1, TimeUnit.HOURS);
                } catch (Exception e) {
                    logger.warning("Failed to cache request: " + e.getMessage());
                }
            }

            // Send Kafka message
            if (kafkaTemplate != null) {
                try {
                    kafkaTemplate.send("request-created", "Request created: " + savedRequest.getId());
                } catch (Exception e) {
                    logger.warning("Failed to send Kafka message: " + e.getMessage());
                }
            }

            return savedRequest;
        } catch (Exception e) {
            logger.severe("Failed to save request: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create request: " + e.getMessage());
        }
    }

    private void validateSender(UUID senderId) {
        if (userServiceUrl == null || userServiceUrl.isEmpty()) {
            logger.warning("User service URL not configured, skipping sender validation");
            return;
        }

        String userUrl = userServiceUrl + "/" + senderId;
        try {
            String userResponse = restTemplate.getForObject(userUrl, String.class);
            if (userResponse == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender not found in external service");
            }
            logger.info("Sender validation successful for ID: " + senderId);
        } catch (Exception e) {
            logger.warning("Sender validation failed for ID " + senderId + ": " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Sender not found: " + e.getMessage());
        }
    }

    private void validateRide(UUID rideId) {
        if (rideServiceUrl == null || rideServiceUrl.isEmpty()) {
            logger.warning("Ride service URL not configured, skipping ride validation");
            return;
        }

        String rideUrl = rideServiceUrl + "/" + rideId;
        try {
            String rideResponse = restTemplate.getForObject(rideUrl, String.class);
            if (rideResponse == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found in external service");
            }
            logger.info("Ride validation successful for ID: " + rideId);
        } catch (Exception e) {
            logger.warning("Ride validation failed for ID " + rideId + ": " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ride not found: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<Request> getRequestsByUser(UUID userId) {
        return requestRepository.findBySenderId(userId);
    }

    @Transactional(readOnly = true)
    public Request getRequestById(UUID requestId) {
        String cacheKey = REQUEST_CACHE_KEY + requestId;
        if (redisTemplate != null) {
            try {
                Request cachedRequest = (Request) redisTemplate.opsForValue().get(cacheKey);
                if (cachedRequest != null) {
                    logger.info("Request found in cache: " + requestId);
                    return cachedRequest;
                }
            } catch (Exception e) {
                logger.warning("Failed to get request from cache: " + e.getMessage());
            }
        }

        Request request = requestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(cacheKey, request, 1, TimeUnit.HOURS);
            } catch (Exception e) {
                logger.warning("Failed to cache request: " + e.getMessage());
            }
        }

        return request;
    }

    @Transactional(readOnly = true)
    public List<Request> getActiveRequestsForDriver(UUID driverId) {
        List<RequestStatus> statuses = List.of(RequestStatus.PENDING, RequestStatus.ACCEPTED);
        return requestRepository.findByAssignedDriverIdAndStatusIn(driverId, statuses);
    }

    public Request updateRequestStatus(UUID requestId, String status, String role) {
        Request request = getRequestById(requestId);
        if (!role.equals("DRIVER")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only drivers can update status");
        }
        RequestStatus newStatus;
        try {
            newStatus = RequestStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status");
        }
        if (newStatus != RequestStatus.ACCEPTED && newStatus != RequestStatus.DECLINED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status");
        }
        request.setStatus(newStatus);
        Request updatedRequest = requestRepository.save(request);

        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(REQUEST_CACHE_KEY + requestId, updatedRequest, 1, TimeUnit.HOURS);
            } catch (Exception e) {
                logger.warning("Failed to update cache: " + e.getMessage());
            }
        }

        if (kafkaTemplate != null) {
            try {
                kafkaTemplate.send("request-status-updated", "Request " + requestId + " status: " + status);
            } catch (Exception e) {
                logger.warning("Failed to send Kafka message: " + e.getMessage());
            }
        }

        return updatedRequest;
    }

    public Request assignDriver(UUID requestId, UUID driverId, String role) {
        if (!role.equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can assign drivers");
        }
        Request request = getRequestById(requestId);

        if (!skipExternalValidation && driverServiceUrl != null && !driverServiceUrl.isEmpty()) {
            validateDriver(driverId);
        }

        // Create and set driver
        Driver driver = new Driver();
        driver.setDriverId(driverId);
        request.setAssignedDriver(driver);
        request.setStatus(RequestStatus.ASSIGNED);

        Request updatedRequest = requestRepository.save(request);

        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(REQUEST_CACHE_KEY + requestId, updatedRequest, 1, TimeUnit.HOURS);
            } catch (Exception e) {
                logger.warning("Failed to update cache: " + e.getMessage());
            }
        }

        if (kafkaTemplate != null) {
            try {
                kafkaTemplate.send("request-status-updated", "Driver assigned to request: " + requestId);
            } catch (Exception e) {
                logger.warning("Failed to send Kafka message: " + e.getMessage());
            }
        }

        return updatedRequest;
    }

    private void validateDriver(UUID driverId) {
        String driverUrl = driverServiceUrl + "/" + driverId;
        try {
            String driverResponse = restTemplate.getForObject(driverUrl, String.class);
            if (driverResponse == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found in external service");
            }
            logger.info("Driver validation successful for ID: " + driverId);
        } catch (Exception e) {
            logger.warning("Driver validation failed for ID " + driverId + ": " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Driver not found: " + e.getMessage());
        }
    }

    public void cancelRequest(UUID requestId, UUID userId) {
        Request request = getRequestById(requestId);
        if (!request.getSender().getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only sender can cancel");
        }
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending requests can be cancelled");
        }
        request.setStatus(RequestStatus.CANCELLED);
        requestRepository.save(request);

        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(REQUEST_CACHE_KEY + requestId, request, 1, TimeUnit.HOURS);
            } catch (Exception e) {
                logger.warning("Failed to update cache: " + e.getMessage());
            }
        }

        if (kafkaTemplate != null) {
            try {
                kafkaTemplate.send("request-status-updated", "Request cancelled: " + requestId);
            } catch (Exception e) {
                logger.warning("Failed to send Kafka message: " + e.getMessage());
            }
        }
    }
}