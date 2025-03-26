package com.sinelec.stage.controller;

import com.sinelec.stage.domain.engine.model.DriverDefinition;
import com.sinelec.stage.service.DriverDefinitionService;
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
@RequestMapping("/api/driver-definitions")
@Tag(name = "Driver Definitions", description = "APIs for managing driver definitions")
public class DriverDefinitionController {
    
    private final DriverDefinitionService driverDefinitionService;
    
    @Autowired
    public DriverDefinitionController(DriverDefinitionService driverDefinitionService) {
        this.driverDefinitionService = driverDefinitionService;
    }
    
    @Operation(summary = "Get all driver definitions", 
               description = "Returns all driver definitions in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved driver definitions",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = DriverDefinition.class)))
    })
    @GetMapping
    public ResponseEntity<List<DriverDefinition>> getAllDriverDefinitions() {
        List<DriverDefinition> driverDefinitions = driverDefinitionService.getAllDriverDefinitions();
        return ResponseEntity.ok(driverDefinitions);
    }
    
    @Operation(summary = "Get driver definition by ID", 
               description = "Returns a driver definition by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved driver definition",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = DriverDefinition.class))),
        @ApiResponse(responseCode = "404", description = "Driver definition not found", 
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<DriverDefinition> getDriverDefinitionById(
            @Parameter(description = "ID of the driver definition to retrieve") 
            @PathVariable String id) {
        return driverDefinitionService.getDriverDefinitionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Create a driver definition", 
               description = "Creates a new driver definition")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Driver definition created successfully",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = DriverDefinition.class))),
        @ApiResponse(responseCode = "400", description = "Invalid driver definition data", 
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<DriverDefinition> createDriverDefinition(
            @Parameter(description = "Driver definition to create") 
            @RequestBody DriverDefinition driverDefinition) {
        DriverDefinition createdDefinition = driverDefinitionService.createDriverDefinition(driverDefinition);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDefinition);
    }
    
    @Operation(summary = "Update a driver definition", 
               description = "Updates an existing driver definition")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Driver definition updated successfully",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = DriverDefinition.class))),
        @ApiResponse(responseCode = "404", description = "Driver definition not found", 
                    content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<DriverDefinition> updateDriverDefinition(
            @Parameter(description = "ID of the driver definition to update") 
            @PathVariable String id, 
            @Parameter(description = "Updated driver definition data") 
            @RequestBody DriverDefinition driverDefinition) {
        return driverDefinitionService.updateDriverDefinition(id, driverDefinition)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Delete a driver definition", 
               description = "Deletes a driver definition by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Driver definition deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Driver definition not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDriverDefinition(
            @Parameter(description = "ID of the driver definition to delete") 
            @PathVariable String id) {
        boolean deleted = driverDefinitionService.deleteDriverDefinition(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
} 