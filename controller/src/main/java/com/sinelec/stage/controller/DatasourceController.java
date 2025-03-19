package com.sinelec.stage.controller;

import com.sinelec.stage.domain.engine.model.Datasource;
import com.sinelec.stage.service.DatasourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/datasources")
public class DatasourceController {
    
    private final DatasourceService datasourceService;
    
    @Autowired
    public DatasourceController(DatasourceService datasourceService) {
        this.datasourceService = datasourceService;
    }
    
    @GetMapping
    public ResponseEntity<List<Datasource>> getAllDatasources() {
        List<Datasource> datasources = datasourceService.getAllDatasources();
        return ResponseEntity.ok(datasources);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Datasource> getDatasourceById(@PathVariable String id) {
        return datasourceService.getDatasourceById(id)
                .map(ResponseEntity::ok)
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
    
    @PostMapping
    public ResponseEntity<?> createDatasource(@RequestBody Datasource datasource) {
        try {
            Datasource createdDatasource = datasourceService.createDatasource(datasource);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdDatasource);
        } catch (IllegalArgumentException e) {
            // Return a 400 Bad Request with the validation error message
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Datasource> updateDatasource(@PathVariable String id, @RequestBody Datasource datasource) {
        try {
            return datasourceService.updateDatasource(id, datasource)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            // Return a 400 Bad Request with the validation error message
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDatasource(@PathVariable String id) {
        boolean deleted = datasourceService.deleteDatasource(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
} 