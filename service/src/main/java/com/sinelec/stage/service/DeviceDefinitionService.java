package com.sinelec.stage.service;

import com.sinelec.stage.domain.engine.model.DeviceDefinition;
import com.sinelec.stage.repository.DeviceDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeviceDefinitionService {
    
    private final DeviceDefinitionRepository deviceDefinitionRepository;
    
    @Autowired
    public DeviceDefinitionService(DeviceDefinitionRepository deviceDefinitionRepository) {
        this.deviceDefinitionRepository = deviceDefinitionRepository;
    }
    
    public List<DeviceDefinition> getAllDeviceDefinitions() {
        return deviceDefinitionRepository.findAll();
    }
    
    public Optional<DeviceDefinition> getDeviceDefinitionById(String id) {
        return deviceDefinitionRepository.findById(id);
    }

    public List<DeviceDefinition> getDeviceDefinitionByIdIn(List<String> ids) {
        return deviceDefinitionRepository.findByIdIn(ids);
    }

    
    public List<DeviceDefinition> getDeviceDefinitionsByName(String name) {
        return deviceDefinitionRepository.findByName(name);
    }
    
    public DeviceDefinition createDeviceDefinition(DeviceDefinition deviceDefinition) {
        return deviceDefinitionRepository.save(deviceDefinition);
    }
    
    public Optional<DeviceDefinition> updateDeviceDefinition(String id, DeviceDefinition deviceDefinitionDetails) {
        return deviceDefinitionRepository.findById(id)
            .map(deviceDefinition -> {
                deviceDefinition.setName(deviceDefinitionDetails.getName());
                deviceDefinition.setDescription(deviceDefinitionDetails.getDescription());
                deviceDefinition.setSignals(deviceDefinitionDetails.getSignals());
                return deviceDefinitionRepository.save(deviceDefinition);
            });
    }
    
    public boolean deleteDeviceDefinition(String id) {
        return deviceDefinitionRepository.findById(id)
            .map(deviceDefinition -> {
                deviceDefinitionRepository.delete(deviceDefinition);
                return true;
            })
            .orElse(false);
    }
} 