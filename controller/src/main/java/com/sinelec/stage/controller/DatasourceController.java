package com.sinelec.stage.controller;

import com.sinelec.stage.domain.engine.model.Datasource;
import com.sinelec.stage.service.DatasourceService;
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
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/datasources")
@Tag(name = "Datasource", description = "Datasource management APIs")
public class DatasourceController {
    
    private final DatasourceService datasourceService;
    
    @Autowired
    public DatasourceController(DatasourceService datasourceService) {
        this.datasourceService = datasourceService;
    }
    
    @Operation(summary = "Get all datasources", description = "Returns a list of all available datasources")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of datasources",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = Datasource.class)))
    })
    @GetMapping
    public ResponseEntity<List<Datasource>> getAllDatasources() {
        List<Datasource> datasources = datasourceService.getAllDatasources();
        return ResponseEntity.ok(datasources);
    }
    
    @Operation(summary = "Get datasource by ID", description = "Returns a datasource based on an ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the datasource",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = Datasource.class))),
        @ApiResponse(responseCode = "404", description = "Datasource not found", 
                    content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Datasource> getDatasourceById(
            @Parameter(description = "ID of the datasource to be retrieved") 
            @PathVariable String id) {
        Optional<Datasource> datasource = datasourceService.getDatasourceById(id);
        return datasource.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<Datasource>> getDatasourcesByDriverId(@PathVariable String driverId) {
        List<Datasource> datasources = datasourceService.getDatasourcesByDriverId(driverId);
        return ResponseEntity.ok(datasources);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<Datasource>> getActiveDatasources() {
        List<Datasource> datasources = datasourceService.getActiveDatasources();
        return ResponseEntity.ok(datasources);
    }
    
    @GetMapping("/connected")
    public ResponseEntity<List<Datasource>> getConnectedDatasources() {
        List<Datasource> datasources = datasourceService.getConnectedDatasources();
        return ResponseEntity.ok(datasources);
    }
    
    @Operation(summary = "Create a new datasource", description = "Creates a new datasource with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Datasource created successfully",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = Datasource.class))),
        @ApiResponse(responseCode = "400", description = "Invalid datasource data", 
                    content = @Content)
    })
    @PostMapping
    public ResponseEntity<Datasource> createDatasource(
            @Parameter(description = "Datasource to be created") 
            @RequestBody Datasource datasource) {
        Datasource createdDatasource = datasourceService.createDatasource(datasource);
        return new ResponseEntity<>(createdDatasource, HttpStatus.CREATED);
    }
    
    @Operation(summary = "Update a datasource", description = "Updates an existing datasource with the provided information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Datasource updated successfully",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = Datasource.class))),
        @ApiResponse(responseCode = "404", description = "Datasource not found", 
                    content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<Datasource> updateDatasource(
            @Parameter(description = "ID of the datasource to be updated") 
            @PathVariable String id,
            @Parameter(description = "Updated datasource information") 
            @RequestBody Datasource datasource) {
        Optional<Datasource> existingDatasource = datasourceService.getDatasourceById(id);
        
        if (existingDatasource.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        datasource.setId(id);
        return datasourceService.updateDatasource(id, datasource)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Delete a datasource", description = "Deletes a datasource based on an ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Datasource deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Datasource not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDatasource(
            @Parameter(description = "ID of the datasource to be deleted") 
            @PathVariable String id) {
        Optional<Datasource> existingDatasource = datasourceService.getDatasourceById(id);
        
        if (existingDatasource.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        datasourceService.deleteDatasource(id);
        return ResponseEntity.noContent().build();
    }
} 