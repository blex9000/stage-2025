package com.sinelec.stage.controller;

import com.sinelec.stage.domain.engine.model.DeviceDefinition;
import com.sinelec.stage.service.DeviceDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/device-definitions")
public class DeviceDefinitionController {
    
    private final DeviceDefinitionService deviceDefinitionService;
    
    @Autowired
    public DeviceDefinitionController(DeviceDefinitionService deviceDefinitionService) {
        this.deviceDefinitionService = deviceDefinitionService;
    }
    
    @GetMapping
    public ResponseEntity<List<DeviceDefinition>> getAllDeviceDefinitions() {
        List<DeviceDefinition> deviceDefinitions = deviceDefinitionService.getAllDeviceDefinitions();
        return ResponseEntity.ok(deviceDefinitions);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DeviceDefinition> getDeviceDefinitionById(@PathVariable String id) {
        return deviceDefinitionService.getDeviceDefinitionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/name/{name}")
    public ResponseEntity<List<DeviceDefinition>> getDeviceDefinitionsByName(@PathVariable String name) {
        List<DeviceDefinition> deviceDefinitions = deviceDefinitionService.getDeviceDefinitionsByName(name);
        return ResponseEntity.ok(deviceDefinitions);
    }
    
    @PostMapping
    public ResponseEntity<DeviceDefinition> createDeviceDefinition(@RequestBody DeviceDefinition deviceDefinition) {
        DeviceDefinition createdDefinition = deviceDefinitionService.createDeviceDefinition(deviceDefinition);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDefinition);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<DeviceDefinition> updateDeviceDefinition(
            @PathVariable String id, @RequestBody DeviceDefinition deviceDefinition) {
        return deviceDefinitionService.updateDeviceDefinition(id, deviceDefinition)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeviceDefinition(@PathVariable String id) {
        boolean deleted = deviceDefinitionService.deleteDeviceDefinition(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
} 