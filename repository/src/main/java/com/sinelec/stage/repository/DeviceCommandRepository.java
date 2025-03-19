package com.sinelec.stage.repository;

import com.sinelec.stage.domain.engine.model.DeviceCommand;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DeviceCommandRepository extends MongoRepository<DeviceCommand, String> {
    
    List<DeviceCommand> findByDeviceId(String deviceId);
    
    List<DeviceCommand> findByDatasourceId(String datasourceId);
    
    List<DeviceCommand> findByStatus(DeviceCommand.CommandStatus status);
        
    List<DeviceCommand> findByCreatedAtBefore(Date date);
} 