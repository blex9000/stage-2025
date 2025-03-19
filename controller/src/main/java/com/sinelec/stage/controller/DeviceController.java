package com.sinelec.stage.controller;

import com.sinelec.stage.domain.engine.model.Device;
import com.sinelec.stage.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {
    
    private final DeviceService deviceService;
    
    @Autowired
    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }
    
    @GetMapping
    public ResponseEntity<List<Device>> getAllDevices() {
        List<Device> devices = deviceService.getAllDevices();
        return ResponseEntity.ok(devices);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(@PathVariable String id) {
        return deviceService.getDeviceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/definition/{deviceDefinitionId}")
    public ResponseEntity<List<Device>> getDevicesByDefinition(@PathVariable String deviceDefinitionId) {
        List<Device> devices = deviceService.getDevicesByDeviceDefinitionId(deviceDefinitionId);
        return ResponseEntity.ok(devices);
    }
    
    @GetMapping("/datasource/{datasourceId}")
    public ResponseEntity<List<Device>> getDevicesByDatasource(@PathVariable String datasourceId) {
        List<Device> devices = deviceService.getDevicesByDatasourceId(datasourceId);
        return ResponseEntity.ok(devices);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<Device>> getActiveDevices() {
        List<Device> devices = deviceService.getActiveDevices();
        return ResponseEntity.ok(devices);
    }
    
    @GetMapping("/location/{location}")
    public ResponseEntity<List<Device>> getDevicesByLocation(@PathVariable String location) {
        List<Device> devices = deviceService.getAllDevices().stream()
            .filter(device -> location.equals(device.getLocation()))
            .toList();
        return ResponseEntity.ok(devices);
    }
    
    @PostMapping
    public ResponseEntity<Device> createDevice(@RequestBody Device device) {
        try {
            Device createdDevice = deviceService.createDevice(device);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDevice);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Device> updateDevice(@PathVariable String id, @RequestBody Device device) {
        try {
            return deviceService.updateDevice(id, device)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable String id) {
        boolean deleted = deviceService.deleteDevice(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
} 