package com.sinelec.stage.repository;

import com.sinelec.stage.domain.engine.model.DriverDefinition;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverDefinitionRepository extends MongoRepository<DriverDefinition, String> {
    Optional<DriverDefinition> findById(String id);
}