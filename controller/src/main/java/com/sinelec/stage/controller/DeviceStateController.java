package com.sinelec.stage.controller;

import com.sinelec.stage.domain.engine.model.DeviceState;
import com.sinelec.stage.domain.engine.model.HealthStatus;
import com.sinelec.stage.service.DeviceStateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/device-states")
public class DeviceStateController {
    
    private final DeviceStateService deviceStateService;
    
    @Autowired
    public DeviceStateController(DeviceStateService deviceStateService) {
        this.deviceStateService = deviceStateService;
    }
    
    @GetMapping
    public ResponseEntity<List<DeviceState>> getAllDeviceStates() {
        List<DeviceState> deviceStates = deviceStateService.getAllDeviceStates();
        return ResponseEntity.ok(deviceStates);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DeviceState> getDeviceStateById(@PathVariable String id) {
        return deviceStateService.getDeviceStateById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/device/{deviceId}")
    public ResponseEntity<DeviceState> getDeviceStateByDeviceId(@PathVariable String deviceId) {
        return deviceStateService.getDeviceStateByDeviceId(deviceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/connected/{connected}")
    public ResponseEntity<List<DeviceState>> getConnectedDeviceStates(@PathVariable boolean connected) {
        List<DeviceState> deviceStates = deviceStateService.getConnectedDeviceStates(connected);
        return ResponseEntity.ok(deviceStates);
    }
    
    @GetMapping("/health-status/{healthStatus}")
    public ResponseEntity<List<DeviceState>> getDeviceStatesByHealthStatus(
            @PathVariable HealthStatus healthStatus) {
        List<DeviceState> deviceStates = deviceStateService.getDeviceStatesByHealthStatus(healthStatus);
        return ResponseEntity.ok(deviceStates);
    }
    
    @PostMapping
    public ResponseEntity<DeviceState> createDeviceState(@RequestBody DeviceState deviceState) {
        DeviceState createdState = deviceStateService.createDeviceState(deviceState);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdState);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<DeviceState> updateDeviceState(
            @PathVariable String id, @RequestBody DeviceState deviceState) {
        return deviceStateService.updateDeviceState(id, deviceState)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/device/{deviceId}/connection")
    public ResponseEntity<DeviceState> updateDeviceConnectionStatus(
            @PathVariable String deviceId, @RequestBody Map<String, Object> connectionUpdate) {
        boolean connected = (boolean) connectionUpdate.get("connected");
        String connectionStatus = (String) connectionUpdate.get("connectionStatus");
        
        return deviceStateService.updateDeviceConnectionStatus(deviceId, connected, connectionStatus)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/device/{deviceId}/health-status")
    public ResponseEntity<DeviceState> updateDeviceHealthStatus(
            @PathVariable String deviceId, @RequestBody Map<String, HealthStatus> healthUpdate) {
        return deviceStateService.updateDeviceHealthStatus(deviceId, healthUpdate.get("healthStatus"))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
} 