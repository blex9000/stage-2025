package com.sinelec.stage.service;

import com.sinelec.stage.domain.engine.model.DeviceState;
import com.sinelec.stage.domain.engine.model.HealthStatus;
import com.sinelec.stage.repository.DeviceStateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class DeviceStateService {
    
    private final DeviceStateRepository deviceStateRepository;
    
    @Autowired
    public DeviceStateService(DeviceStateRepository deviceStateRepository) {
        this.deviceStateRepository = deviceStateRepository;
    }
    
    public List<DeviceState> getAllDeviceStates() {
        return deviceStateRepository.findAll();
    }
    
    public Optional<DeviceState> getDeviceStateById(String id) {
        return deviceStateRepository.findById(id);
    }
    
    public Optional<DeviceState> getDeviceStateByDeviceId(String deviceId) {
        return deviceStateRepository.findByDeviceId(deviceId);
    }
    
    public List<DeviceState> getConnectedDeviceStates(boolean connected) {
        return deviceStateRepository.findByConnected(connected);
    }
    
    public List<DeviceState> getDeviceStatesByHealthStatus(HealthStatus healthStatus) {
        return deviceStateRepository.findByHealthStatus(healthStatus);
    }
    
    public DeviceState createDeviceState(DeviceState deviceState) {
        deviceState.setCreated(new Date());
        deviceState.setUpdated(new Date());
        return deviceStateRepository.save(deviceState);
    }
    
    public Optional<DeviceState> updateDeviceState(String id, DeviceState deviceStateDetails) {
        return deviceStateRepository.findById(id)
            .map(deviceState -> {
                // Update state properties
                deviceState.setConnected(deviceStateDetails.isConnected());
                deviceState.setConnectionStatus(deviceStateDetails.getConnectionStatus());
                deviceState.setHealthStatus(deviceStateDetails.getHealthStatus());
                deviceState.setSignalStates(deviceStateDetails.getSignalStates());
                deviceState.setUpdated(new Date());
                
                return deviceStateRepository.save(deviceState);
            });
    }
    
    public Optional<DeviceState> updateDeviceConnectionStatus(String deviceId, boolean connected, String connectionStatus) {
        return deviceStateRepository.findByDeviceId(deviceId)
            .map(deviceState -> {
                deviceState.setConnected(connected);
                deviceState.setConnectionStatus(connectionStatus);
                deviceState.setUpdated(new Date());
                return deviceStateRepository.save(deviceState);
            });
    }
    
    public Optional<DeviceState> updateDeviceHealthStatus(String deviceId, HealthStatus healthStatus) {
        return deviceStateRepository.findByDeviceId(deviceId)
            .map(deviceState -> {
                deviceState.setHealthStatus(healthStatus);
                deviceState.setUpdated(new Date());
                return deviceStateRepository.save(deviceState);
            });
    }
} 