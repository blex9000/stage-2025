package com.sinelec.stage.controller;

import com.sinelec.stage.domain.engine.model.DeviceDefinition;
import com.sinelec.stage.service.DeviceDefinitionService;
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
@RequestMapping("/api/device-definitions")
@Tag(name = "Device Definitions", description = "APIs for managing device definitions and their signal configurations")
public class DeviceDefinitionController {
    
    private final DeviceDefinitionService deviceDefinitionService;
    
    @Autowired
    public DeviceDefinitionController(DeviceDefinitionService deviceDefinitionService) {
        this.deviceDefinitionService = deviceDefinitionService;
    }
    
    @Operation(summary = "Get all device definitions", 
               description = "Returns all device definitions in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved device definitions",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = DeviceDefinition.class)))
    })
    @GetMapping
    public ResponseEntity<List<DeviceDefinition>> getAllDeviceDefinitions() {
        List<DeviceDefinition> deviceDefinitions = deviceDefinitionService.getAllDeviceDefinitions();
        return ResponseEntity.ok(deviceDefinitions);
    }
    
    @Operation(summary = "Get device definition by ID", 
               description = "Returns a device definition by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved device definition",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = DeviceDefinition.class))),
        @ApiResponse(responseCode = "404", description = "Device definition not found", 
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<DeviceDefinition> getDeviceDefinitionById(
            @Parameter(description = "ID of the device definition to retrieve") 
            @PathVariable String id) {
        return deviceDefinitionService.getDeviceDefinitionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Get device definitions by name", 
               description = "Returns device definitions that match the specified name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved device definitions")
    })
    @GetMapping("/name/{name}")
    public ResponseEntity<List<DeviceDefinition>> getDeviceDefinitionsByName(
            @Parameter(description = "Name to search for") 
            @PathVariable String name) {
        List<DeviceDefinition> deviceDefinitions = deviceDefinitionService.getDeviceDefinitionsByName(name);
        return ResponseEntity.ok(deviceDefinitions);
    }
    
    @Operation(summary = "Create a device definition", 
               description = "Creates a new device definition")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Device definition created successfully",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = DeviceDefinition.class))),
        @ApiResponse(responseCode = "400", description = "Invalid device definition data", 
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<DeviceDefinition> createDeviceDefinition(
            @Parameter(description = "Device definition to create") 
            @RequestBody DeviceDefinition deviceDefinition) {
        DeviceDefinition createdDefinition = deviceDefinitionService.createDeviceDefinition(deviceDefinition);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDefinition);
    }
    
    @Operation(summary = "Update a device definition", 
               description = "Updates an existing device definition")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Device definition updated successfully",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = DeviceDefinition.class))),
        @ApiResponse(responseCode = "404", description = "Device definition not found", 
                    content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<DeviceDefinition> updateDeviceDefinition(
            @Parameter(description = "ID of the device definition to update") 
            @PathVariable String id, 
            @Parameter(description = "Updated device definition data") 
            @RequestBody DeviceDefinition deviceDefinition) {
        return deviceDefinitionService.updateDeviceDefinition(id, deviceDefinition)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Delete a device definition", 
               description = "Deletes a device definition by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Device definition deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Device definition not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeviceDefinition(
            @Parameter(description = "ID of the device definition to delete") 
            @PathVariable String id) {
        boolean deleted = deviceDefinitionService.deleteDeviceDefinition(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
} 