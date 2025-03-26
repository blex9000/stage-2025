package com.sinelec.stage.controller;

import com.sinelec.stage.domain.engine.model.Reading;
import com.sinelec.stage.service.ReadingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/readings")
@Tag(name = "Readings", description = "APIs for managing and retrieving device readings")
public class ReadingController {
    
    private final ReadingService readingService;
    
    @Autowired
    public ReadingController(ReadingService readingService) {
        this.readingService = readingService;
    }
    
    @Operation(summary = "Save a reading", description = "Saves a single reading")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Reading saved successfully",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = Reading.class)))
    })
    @PostMapping
    public ResponseEntity<Reading> saveReading(
            @Parameter(description = "Reading to save") 
            @RequestBody Reading reading) {
        Reading savedReading = readingService.saveReading(reading);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedReading);
    }
    
    @Operation(summary = "Save multiple readings", description = "Saves a batch of readings")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Readings saved successfully",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = Reading.class)))
    })
    @PostMapping("/batch")
    public ResponseEntity<List<Reading>> saveReadings(
            @Parameter(description = "Readings to save") 
            @RequestBody List<Reading> readings) {
        List<Reading> savedReadings = readingService.saveReadings(readings);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedReadings);
    }
    
    @Operation(summary = "Get reading by ID", description = "Returns a reading by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reading found",
                    content = @Content(mediaType = "application/json", 
                    schema = @Schema(implementation = Reading.class))),
        @ApiResponse(responseCode = "404", description = "Reading not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<Reading> getReadingById(
            @Parameter(description = "ID of the reading to retrieve") 
            @PathVariable String id) {
        return readingService.getReadingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Get readings by device and signal", description = "Returns readings for a specific device and signal")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Readings retrieved successfully")
    })
    @GetMapping("/device/{deviceId}/signal/{signalId}")
    public ResponseEntity<List<Reading>> getReadingsByDeviceAndSignal(
            @Parameter(description = "ID of the device") 
            @PathVariable String deviceId, 
            @Parameter(description = "ID of the signal") 
            @PathVariable String signalId) {
        List<Reading> readings = readingService.getReadingsByDeviceAndSignal(deviceId, signalId);
        return ResponseEntity.ok(readings);
    }
    
    @Operation(summary = "Get readings by device and signal in time range", description = "Returns readings for a specific device and signal within a time range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Readings retrieved successfully")
    })
    @GetMapping("/device/{deviceId}/signal/{signalId}/timerange")
    public ResponseEntity<List<Reading>> getReadingsByDeviceAndSignalInTimeRange(
            @Parameter(description = "ID of the device") 
            @PathVariable String deviceId, 
            @Parameter(description = "ID of the signal") 
            @PathVariable String signalId,
            @Parameter(description = "Start time (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startTime,
            @Parameter(description = "End time (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endTime) {
        List<Reading> readings = readingService.getReadingsByDeviceAndSignalInTimeRange(
                deviceId, signalId, startTime, endTime);
        return ResponseEntity.ok(readings);
    }
    
    @Operation(summary = "Get paginated readings by device and signal in time range", description = "Returns paginated readings for a specific device and signal within a time range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Readings retrieved successfully")
    })
    @GetMapping("/device/{deviceId}/signal/{signalId}/timerange/paginated")
    public ResponseEntity<Page<Reading>> getReadingsByDeviceAndSignalInTimeRangePaginated(
            @Parameter(description = "ID of the device") 
            @PathVariable String deviceId, 
            @Parameter(description = "ID of the signal") 
            @PathVariable String signalId,
            @Parameter(description = "Start time (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startTime,
            @Parameter(description = "End time (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endTime,
            @Parameter(description = "Pagination parameters") 
            Pageable pageable) {
        Page<Reading> readings = readingService.getReadingsByDeviceAndSignalInTimeRangePaginated(
                deviceId, signalId, startTime, endTime, pageable);
        return ResponseEntity.ok(readings);
    }
    
    @Operation(summary = "Get readings by meta ID in time range", description = "Returns readings for a specific meta ID within a time range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Readings retrieved successfully")
    })
    @GetMapping("/meta/{metaId}/timerange")
    public ResponseEntity<List<Reading>> getReadingsByMetaIdInTimeRange(
            @Parameter(description = "Meta ID") 
            @PathVariable String metaId,
            @Parameter(description = "Start time (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startTime,
            @Parameter(description = "End time (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endTime) {
        List<Reading> readings = readingService.getReadingsByMetaIdInTimeRange(metaId, startTime, endTime);
        return ResponseEntity.ok(readings);
    }
    
    @Operation(summary = "Get alarm readings", description = "Returns readings in alarm state")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Alarm readings retrieved successfully")
    })
    @GetMapping("/alarms")
    public ResponseEntity<List<Reading>> getAlarmReadings(
            @Parameter(description = "Filter for readings in alarm (true) or not in alarm (false)") 
            @RequestParam(defaultValue = "true") boolean inAlarm) {
        List<Reading> readings = readingService.getAlarmReadings(inAlarm);
        return ResponseEntity.ok(readings);
    }
} 