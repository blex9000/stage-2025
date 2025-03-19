package com.sinelec.stage.controller;

import com.sinelec.stage.domain.engine.model.DeviceCommand;
import com.sinelec.stage.service.DeviceCommandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/commands")
public class DeviceCommandController {
    
    private final DeviceCommandService deviceCommandService;
    
    @Autowired
    public DeviceCommandController(DeviceCommandService deviceCommandService) {
        this.deviceCommandService = deviceCommandService;
    }
    
    @GetMapping
    public ResponseEntity<List<DeviceCommand>> getAllCommands() {
        List<DeviceCommand> commands = deviceCommandService.getAllCommands();
        return ResponseEntity.ok(commands);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DeviceCommand> getCommandById(@PathVariable String id) {
        return deviceCommandService.getCommandById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/device/{deviceId}")
    public ResponseEntity<List<DeviceCommand>> getCommandsByDeviceId(@PathVariable String deviceId) {
        List<DeviceCommand> commands = deviceCommandService.getCommandsByDeviceId(deviceId);
        return ResponseEntity.ok(commands);
    }
    
    @GetMapping("/datasource/{datasourceId}")
    public ResponseEntity<List<DeviceCommand>> getCommandsByDatasourceId(@PathVariable String datasourceId) {
        List<DeviceCommand> commands = deviceCommandService.getCommandsByDatasourceId(datasourceId);
        return ResponseEntity.ok(commands);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<DeviceCommand>> getCommandsByStatus(
            @PathVariable DeviceCommand.CommandStatus status) {
        List<DeviceCommand> commands = deviceCommandService.getCommandsByStatus(status);
        return ResponseEntity.ok(commands);
    }

    
    @GetMapping("/older-than")
    public ResponseEntity<List<DeviceCommand>> getOldCommands(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date cutoffDate) {
        List<DeviceCommand> commands = deviceCommandService.getOldCommands(cutoffDate);
        return ResponseEntity.ok(commands);
    }
    
    @PostMapping
    public ResponseEntity<DeviceCommand> createCommand(@RequestBody DeviceCommand command) {
        try {
            // Generate an ID if not present (this is often a common issue)
            if (command.getId() == null || command.getId().isEmpty()) {
                command.setId(UUID.randomUUID().toString());
            }
            
            DeviceCommand createdCommand = deviceCommandService.createCommand(command);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCommand);
        } catch (Exception e) {
            // Log the exception for debugging
            e.printStackTrace();
            throw e;
        }
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<DeviceCommand> updateCommandStatus(
            @PathVariable String id, @RequestBody Map<String, DeviceCommand.CommandStatus> statusUpdate) {
        return deviceCommandService.updateCommandStatus(id, statusUpdate.get("status"))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}/result")
    public ResponseEntity<DeviceCommand> updateCommandResult(
            @PathVariable String id, 
            @RequestParam DeviceCommand.CommandStatus status,
            @RequestBody Map<String, String> resultUpdate) {
        return deviceCommandService.updateCommandResult(id, status, resultUpdate.get("resultMessage"))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
} 