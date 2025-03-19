package com.sinelec.stage.repository;

import com.sinelec.stage.domain.engine.model.DeviceDefinition;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeviceDefinitionRepository extends MongoRepository<DeviceDefinition, String> {
    
    List<DeviceDefinition> findByName(String name);
} 