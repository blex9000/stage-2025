package com.sinelec.stage.repository;

import com.sinelec.stage.domain.engine.model.Device;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceRepository extends MongoRepository<Device, String> {
    
    List<Device> findByDeviceDefinitionId(String deviceDefinitionId);
    
    List<Device> findByDatasourceId(String datasourceId);
    
    List<Device> findByActive(boolean active);
    
    List<Device> findByLocation(String location);
} 