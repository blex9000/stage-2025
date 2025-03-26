package com.sinelec.stage.engine.controller;

import com.sinelec.stage.domain.engine.model.DeviceCommand;
import com.sinelec.stage.domain.engine.model.Reading;
import com.sinelec.stage.engine.core.Engines;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/engines")
@Tag(name = "Engine", description = "Engine management and control APIs")
public class EngineController {
    
    private final Engines enginesManager;
    
    @Autowired
    public EngineController(Engines enginesManager) {
        this.enginesManager = enginesManager;
    }
    
    @Operation(summary = "Get active engines", description = "Returns a list of all active engine IDs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved active engines")
    })
    @GetMapping
    public ResponseEntity<List<String>> getActiveEngines() {
        return ResponseEntity.ok(enginesManager.getActiveEngineIds());
    }
    
    @Operation(summary = "Get all engine status", description = "Returns status (connected/disconnected) for all engines")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved engine status")
    })
    @GetMapping("/status")
    public ResponseEntity<Map<String, Boolean>> getAllEngineStatus() {
        return ResponseEntity.ok(enginesManager.getAllEngineStatus());
    }
    
    @Operation(summary = "Get all engine statistics", description = "Returns detailed statistics for all engines")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved engine statistics")
    })
    @GetMapping("/stats")
    public ResponseEntity<List<Map<String, Object>>> getAllEngineStats() {
        return ResponseEntity.ok(enginesManager.getAllEngineStatistics());
    }
    
    @Operation(summary = "Get engine statistics by datasource ID", description = "Returns detailed statistics for a specific engine")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved engine statistics"),
        @ApiResponse(responseCode = "404", description = "Engine not found", content = @Content)
    })
    @GetMapping("/{datasourceId}/stats")
    public ResponseEntity<Map<String, Object>> getEngineStats(
            @Parameter(description = "ID of the datasource/engine") 
            @PathVariable String datasourceId) {
        Map<String, Object> stats = enginesManager.getEngineStatistics(datasourceId);
        if (stats.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(stats);
    }
    
    @Operation(summary = "Load and start all engines", description = "Load and start engines for all active datasources")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Operation completed")
    })
    @PostMapping
    public ResponseEntity<Void> loadAndStartAllEngines() {
        enginesManager.loadAndStartEngines();
        return ResponseEntity.ok().build();
    }
    
    @Operation(summary = "Start engine", description = "Start or restart an engine for a specific datasource")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Engine start operation result")
    })
    @PostMapping("/{datasourceId}/start")
    public ResponseEntity<Boolean> startEngine(
            @Parameter(description = "ID of the datasource/engine to start") 
            @PathVariable String datasourceId) {
        return ResponseEntity.ok(enginesManager.restartEngine(datasourceId));
    }
    
    @Operation(summary = "Stop engine", description = "Stop an engine for a specific datasource")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Engine stop operation result")
    })
    @PostMapping("/{datasourceId}/stop")
    public ResponseEntity<Boolean> stopEngine(
            @Parameter(description = "ID of the datasource/engine to stop") 
            @PathVariable String datasourceId) {
        return ResponseEntity.ok(enginesManager.stopEngine(datasourceId));
    }
    
    @Operation(summary = "Poll engine", description = "Poll an engine to get readings from all its devices")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved readings",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = Reading.class)))
    })
    @PostMapping("/{datasourceId}/poll")
    public ResponseEntity<List<Reading>> pollEngine(
            @Parameter(description = "ID of the datasource/engine to poll") 
            @PathVariable String datasourceId) {
        List<Reading> readings = enginesManager.pollEngine(datasourceId);
        return ResponseEntity.ok(readings);
    }
    
    @Operation(summary = "Poll specific devices", description = "Poll only specific devices to get their readings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved readings",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = Reading.class)))
    })
    @PostMapping("/poll/devices")
    public ResponseEntity<List<Reading>> pollDevices(
            @Parameter(description = "IDs of devices to poll") 
            @RequestBody List<String> deviceIds) {
        List<Reading> readings = enginesManager.pollDevices(deviceIds);
        return ResponseEntity.ok(readings);
    }
  
    @Operation(summary = "Execute device command", description = "Execute a command on a device through its engine")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Command executed, returned readings (if any)",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = Reading.class)))
    })
    @PostMapping("/cmd/execute")
    public ResponseEntity<List<Reading>> executeCommand(
            @Parameter(description = "Command to execute") 
            @RequestBody DeviceCommand command) {
        List<Reading> readings = enginesManager.executeCommand(command);
        return ResponseEntity.ok(readings);
    }
} 