package com.sinelec.stage.repository;

import com.sinelec.stage.domain.engine.model.Datasource;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatasourceRepository extends MongoRepository<Datasource, String> {
    
    List<Datasource> findByDriverId(String driverId);
    
    List<Datasource> findByActive(boolean active);
    
    List<Datasource> findByConnected(boolean connected);
} 