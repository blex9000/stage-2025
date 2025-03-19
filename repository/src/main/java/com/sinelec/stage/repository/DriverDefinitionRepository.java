package com.sinelec.stage.repository;

import com.sinelec.stage.domain.engine.model.DriverDefinition;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverDefinitionRepository extends MongoRepository<DriverDefinition, String> {
    List<DriverDefinition> findByName(String name);
    List<DriverDefinition> findByType(DriverDefinition.DriverType type);
} 