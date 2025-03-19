package com.sinelec.stage.service;

import com.sinelec.stage.domain.engine.model.DriverDefinition;
import com.sinelec.stage.repository.DriverDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class DriverDefinitionService {
    
    private final DriverDefinitionRepository driverDefinitionRepository;
    
    @Autowired
    public DriverDefinitionService(DriverDefinitionRepository driverDefinitionRepository) {
        this.driverDefinitionRepository = driverDefinitionRepository;
    }
    
    public List<DriverDefinition> getAllDriverDefinitions() {
        return driverDefinitionRepository.findAll();
    }
    
    public Optional<DriverDefinition> getDriverDefinitionById(String id) {
        return driverDefinitionRepository.findById(id);
    }
    
    public List<DriverDefinition> getDriverDefinitionsByName(String name) {
        return driverDefinitionRepository.findByName(name);
    }
    
    public List<DriverDefinition> getDriverDefinitionsByType(DriverDefinition.DriverType type) {
        return driverDefinitionRepository.findByType(type);
    }
    
    public DriverDefinition createDriverDefinition(DriverDefinition driverDefinition) {
        Date now = new Date();
        driverDefinition.setCreatedAt(now);
        driverDefinition.setUpdatedAt(now);
        return driverDefinitionRepository.save(driverDefinition);
    }
    
    public Optional<DriverDefinition> updateDriverDefinition(String id, DriverDefinition driverDefinitionDetails) {
        return driverDefinitionRepository.findById(id)
            .map(driverDefinition -> {
                driverDefinition.setName(driverDefinitionDetails.getName());
                driverDefinition.setDescription(driverDefinitionDetails.getDescription());
                driverDefinition.setVersion(driverDefinitionDetails.getVersion());
                driverDefinition.setType(driverDefinitionDetails.getType());
                driverDefinition.setConfigurationProperties(driverDefinitionDetails.getConfigurationProperties());
                driverDefinition.setUpdatedAt(new Date());
                return driverDefinitionRepository.save(driverDefinition);
            });
    }
    
    public boolean deleteDriverDefinition(String id) {
        return driverDefinitionRepository.findById(id)
            .map(driverDefinition -> {
                driverDefinitionRepository.delete(driverDefinition);
                return true;
            })
            .orElse(false);
    }
} 