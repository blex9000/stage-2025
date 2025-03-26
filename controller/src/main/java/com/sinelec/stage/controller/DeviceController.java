package com.sinelec.stage.controller;

import com.sinelec.stage.domain.engine.model.Device;
import com.sinelec.stage.service.DeviceService;
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

@RestController
@RequestMapping("/api/devices")
@Tag(name = "Device", description = "Device management APIs")
public class DeviceController {
    
    private final DeviceService deviceService;
    
    @Autowired
    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }
    
    @Operation(summary = "Get all devices", description = "Returns a list of all devices")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved devices",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = Device.class)))
    })
    @GetMapping
    public ResponseEntity<List<Device>> getAllDevices() {
        List<Device> devices = deviceService.getAllDevices();
        return ResponseEntity.ok(devices);
    }
    
    @Operation(summary = "Get device by ID", description = "Returns a device by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved device",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = Device.class))),
        @ApiResponse(responseCode = "404", description = "Device not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(
            @Parameter(description = "ID of the device to retrieve") 
            @PathVariable String id) {
        return deviceService.getDeviceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Get devices by definition", description = "Returns devices with the specified device definition")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved devices")
    })
    @GetMapping("/definition/{deviceDefinitionId}")
    public ResponseEntity<List<Device>> getDevicesByDefinition(
            @Parameter(description = "ID of the device definition") 
            @PathVariable String deviceDefinitionId) {
        List<Device> devices = deviceService.getDevicesByDeviceDefinitionId(deviceDefinitionId);
        return ResponseEntity.ok(devices);
    }
    
    @Operation(summary = "Get devices by datasource", description = "Returns devices connected to the specified datasource")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved devices")
    })
    @GetMapping("/datasource/{datasourceId}")
    public ResponseEntity<List<Device>> getDevicesByDatasource(
            @Parameter(description = "ID of the datasource") 
            @PathVariable String datasourceId) {
        List<Device> devices = deviceService.getDevicesByDatasourceId(datasourceId);
        return ResponseEntity.ok(devices);
    }
    
    @Operation(summary = "Get active devices", description = "Returns all active devices")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved active devices")
    })
    @GetMapping("/active")
    public ResponseEntity<List<Device>> getActiveDevices() {
        List<Device> devices = deviceService.getActiveDevices();
        return ResponseEntity.ok(devices);
    }
    
    @Operation(summary = "Get devices by location", description = "Returns devices at the specified location")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved devices")
    })
    @GetMapping("/location/{location}")
    public ResponseEntity<List<Device>> getDevicesByLocation(
            @Parameter(description = "Location to filter devices by") 
            @PathVariable String location) {
        List<Device> devices = deviceService.getAllDevices().stream()
            .filter(device -> location.equals(device.getLocation()))
            .toList();
        return ResponseEntity.ok(devices);
    }
    
    @Operation(summary = "Create a device", description = "Creates a new device")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Device created successfully",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = Device.class))),
        @ApiResponse(responseCode = "400", description = "Invalid device data", content = @Content)
    })
    @PostMapping
    public ResponseEntity<Device> createDevice(
            @Parameter(description = "Device to create") 
            @RequestBody Device device) {
        try {
            Device createdDevice = deviceService.createDevice(device);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDevice);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(summary = "Update a device", description = "Updates an existing device")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Device updated successfully",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = Device.class))),
        @ApiResponse(responseCode = "404", description = "Device not found", content = @Content),
        @ApiResponse(responseCode = "400", description = "Invalid device data", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<Device> updateDevice(
            @Parameter(description = "ID of the device to update") 
            @PathVariable String id, 
            @Parameter(description = "Updated device data") 
            @RequestBody Device device) {
        try {
            return deviceService.updateDevice(id, device)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(summary = "Delete a device", description = "Deletes a device by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Device deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Device not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(
            @Parameter(description = "ID of the device to delete") 
            @PathVariable String id) {
        boolean deleted = deviceService.deleteDevice(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
} 