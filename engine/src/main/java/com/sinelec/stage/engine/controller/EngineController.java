package com.sinelec.stage.engine.controller;

import com.sinelec.stage.domain.engine.model.DeviceCommand;
import com.sinelec.stage.domain.engine.model.Reading;
import com.sinelec.stage.engine.core.Engines;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/engines")
public class EngineController {
    
    private final Engines enginesManager;
    
    @Autowired
    public EngineController(Engines enginesManager) {
        this.enginesManager = enginesManager;
    }
    
    @GetMapping
    public ResponseEntity<List<String>> getActiveEngines() {
        return ResponseEntity.ok(enginesManager.getActiveEngineIds());
    }
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> getAllEngineStatus() {
        return ResponseEntity.ok(enginesManager.getAllEngineStatus());
    }
    
    @GetMapping("/stats")
    public ResponseEntity<List<Map<String, Object>>> getAllEngineStats() {
        return ResponseEntity.ok(enginesManager.getAllEngineStatistics());
    }
    
    @GetMapping("/{datasourceId}/stats")
    public ResponseEntity<Map<String, Object>> getEngineStats(@PathVariable String datasourceId) {
        Map<String, Object> stats = enginesManager.getEngineStatistics(datasourceId);
        if (stats.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stats);
    }
    
    @PostMapping
    public ResponseEntity<Void> loadAndStartAllEngines() {
        enginesManager.loadAndStartEngines();
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/{datasourceId}/start")
    public ResponseEntity<Boolean> startEngine(@PathVariable String datasourceId) {
        return ResponseEntity.ok(enginesManager.restartEngine(datasourceId));
    }
    
    @PostMapping("/{datasourceId}/stop")
    public ResponseEntity<Boolean> stopEngine(@PathVariable String datasourceId) {
        return ResponseEntity.ok(enginesManager.stopEngine(datasourceId));
    }
    
    @PostMapping("/{datasourceId}/poll")
    public ResponseEntity<List<Reading>> pollEngine(@PathVariable String datasourceId) {
        List<Reading> readings = enginesManager.pollEngine(datasourceId);
        return ResponseEntity.ok(readings);
    }
    
    @PostMapping("/poll/devices")
    public ResponseEntity<List<Reading>> pollDevices(@RequestBody List<String> deviceIds) {
        List<Reading> readings = enginesManager.pollDevices(deviceIds);
        return ResponseEntity.ok(readings);
    }
  
    
    @PostMapping("/write")
    public ResponseEntity<Boolean> writeCommand(@RequestBody DeviceCommand command) {
        boolean success = enginesManager.writeCommand(command);
        return ResponseEntity.ok(success);
    }
} 