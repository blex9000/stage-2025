package com.sinelec.stage.controller;

import com.sinelec.stage.domain.engine.model.DeviceState;
import com.sinelec.stage.domain.engine.model.HealthStatus;
import com.sinelec.stage.service.DeviceStateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/device-states")
@Tag(name = "Device State", description = "APIs for managing device state information")
public class DeviceStateController {
    
    private final DeviceStateService deviceStateService;
    
    @Autowired
    public DeviceStateController(DeviceStateService deviceStateService) {
        this.deviceStateService = deviceStateService;
    }
    
    @Operation(summary = "Get all device states", description = "Returns all device states in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved device states",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = DeviceState.class)))
    })
    @GetMapping
    public ResponseEntity<List<DeviceState>> getAllDeviceStates() {
        List<DeviceState> deviceStates = deviceStateService.getAllDeviceStates();
        return ResponseEntity.ok(deviceStates);
    }
    
    @Operation(summary = "Get device state by ID", description = "Returns a device state by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved device state",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = DeviceState.class))),
        @ApiResponse(responseCode = "404", description = "Device state not found", 
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<DeviceState> getDeviceStateById(
            @Parameter(description = "ID of the device state to retrieve") 
            @PathVariable String id) {
        return deviceStateService.getDeviceStateById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Get device state by device ID", description = "Returns the state of a specific device")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved device state",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = DeviceState.class))),
        @ApiResponse(responseCode = "404", description = "Device state not found", 
                    content = @Content)
    })
    @GetMapping("/device/{deviceId}")
    public ResponseEntity<DeviceState> getDeviceStateByDeviceId(
            @Parameter(description = "ID of the device") 
            @PathVariable String deviceId) {
        return deviceStateService.getDeviceStateByDeviceId(deviceId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Get device states by connection status", 
               description = "Returns device states filtered by connection status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved device states")
    })
    @GetMapping("/connected/{connected}")
    public ResponseEntity<List<DeviceState>> getConnectedDeviceStates(
            @Parameter(description = "Connection status to filter by (true/false)") 
            @PathVariable boolean connected) {
        List<DeviceState> deviceStates = deviceStateService.getConnectedDeviceStates(connected);
        return ResponseEntity.ok(deviceStates);
    }
    
    @Operation(summary = "Get device states by health status", 
               description = "Returns device states filtered by health status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved device states")
    })
    @GetMapping("/health-status/{healthStatus}")
    public ResponseEntity<List<DeviceState>> getDeviceStatesByHealthStatus(
            @Parameter(description = "Health status to filter by (OK, WARNING, CRITICAL, etc.)") 
            @PathVariable HealthStatus healthStatus) {
        List<DeviceState> deviceStates = deviceStateService.getDeviceStatesByHealthStatus(healthStatus);
        return ResponseEntity.ok(deviceStates);
    }
    
    @Operation(summary = "Create a device state", description = "Creates a new device state record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Device state created successfully",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = DeviceState.class)))
    })
    @PostMapping
    public ResponseEntity<DeviceState> createDeviceState(
            @Parameter(description = "Device state to create") 
            @RequestBody DeviceState deviceState) {
        DeviceState createdState = deviceStateService.createDeviceState(deviceState);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdState);
    }
    
    @Operation(summary = "Update a device state", description = "Updates an existing device state")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Device state updated successfully",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = DeviceState.class))),
        @ApiResponse(responseCode = "404", description = "Device state not found", 
                    content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<DeviceState> updateDeviceState(
            @Parameter(description = "ID of the device state to update") 
            @PathVariable String id, 
            @Parameter(description = "Updated device state data") 
            @RequestBody DeviceState deviceState) {
        return deviceStateService.updateDeviceState(id, deviceState)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Update device connection status", 
               description = "Updates the connection status of a specific device")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Connection status updated successfully",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = DeviceState.class))),
        @ApiResponse(responseCode = "404", description = "Device state not found", 
                    content = @Content)
    })
    @PutMapping("/device/{deviceId}/connection")
    public ResponseEntity<DeviceState> updateDeviceConnectionStatus(
            @Parameter(description = "ID of the device") 
            @PathVariable String deviceId, 
            @Parameter(description = "Connection status details") 
            @RequestBody Map<String, Object> connectionUpdate) {
        boolean connected = (boolean) connectionUpdate.get("connected");
        String connectionStatus = (String) connectionUpdate.get("connectionStatus");
        
        return deviceStateService.updateDeviceConnectionStatus(deviceId, connected, connectionStatus)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Update device health status", 
               description = "Updates the health status of a specific device")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Health status updated successfully",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = DeviceState.class))),
        @ApiResponse(responseCode = "404", description = "Device state not found", 
                    content = @Content)
    })
    @PutMapping("/device/{deviceId}/health-status")
    public ResponseEntity<DeviceState> updateDeviceHealthStatus(
            @Parameter(description = "ID of the device") 
            @PathVariable String deviceId, 
            @Parameter(description = "Health status update") 
            @RequestBody Map<String, HealthStatus> healthUpdate) {
        return deviceStateService.updateDeviceHealthStatus(deviceId, healthUpdate.get("healthStatus"))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
} 