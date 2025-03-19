package com.sinelec.stage.repository;

import com.sinelec.stage.domain.engine.model.DeviceState;
import com.sinelec.stage.domain.engine.model.HealthStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceStateRepository extends MongoRepository<DeviceState, String> {
    
    Optional<DeviceState> findByDeviceId(String deviceId);
    
    List<DeviceState> findByConnected(boolean connected);
    
    List<DeviceState> findByHealthStatus(HealthStatus healthStatus);
} 