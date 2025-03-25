package com.sinelec.stage.repository;

import com.sinelec.stage.domain.engine.model.DeviceDefinition;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceDefinitionRepository extends MongoRepository<DeviceDefinition, String> {
    
    List<DeviceDefinition> findByName(String name);
    List<DeviceDefinition> findByIdIn(List<String> ids);
} 