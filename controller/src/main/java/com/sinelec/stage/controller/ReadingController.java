package com.sinelec.stage.controller;

import com.sinelec.stage.domain.engine.model.Reading;
import com.sinelec.stage.service.ReadingService;
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
public class ReadingController {
    
    private final ReadingService readingService;
    
    @Autowired
    public ReadingController(ReadingService readingService) {
        this.readingService = readingService;
    }
    
    @PostMapping
    public ResponseEntity<Reading> saveReading(@RequestBody Reading reading) {
        Reading savedReading = readingService.saveReading(reading);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedReading);
    }
    
    @PostMapping("/batch")
    public ResponseEntity<List<Reading>> saveReadings(@RequestBody List<Reading> readings) {
        List<Reading> savedReadings = readingService.saveReadings(readings);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedReadings);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Reading> getReadingById(@PathVariable String id) {
        return readingService.getReadingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/device/{deviceId}/signal/{signalId}")
    public ResponseEntity<List<Reading>> getReadingsByDeviceAndSignal(
            @PathVariable String deviceId, @PathVariable String signalId) {
        List<Reading> readings = readingService.getReadingsByDeviceAndSignal(deviceId, signalId);
        return ResponseEntity.ok(readings);
    }
    
    @GetMapping("/device/{deviceId}/signal/{signalId}/timerange")
    public ResponseEntity<List<Reading>> getReadingsByDeviceAndSignalInTimeRange(
            @PathVariable String deviceId, 
            @PathVariable String signalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endTime) {
        List<Reading> readings = readingService.getReadingsByDeviceAndSignalInTimeRange(
                deviceId, signalId, startTime, endTime);
        return ResponseEntity.ok(readings);
    }
    
    @GetMapping("/device/{deviceId}/signal/{signalId}/timerange/paginated")
    public ResponseEntity<Page<Reading>> getReadingsByDeviceAndSignalInTimeRangePaginated(
            @PathVariable String deviceId, 
            @PathVariable String signalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endTime,
            Pageable pageable) {
        Page<Reading> readings = readingService.getReadingsByDeviceAndSignalInTimeRangePaginated(
                deviceId, signalId, startTime, endTime, pageable);
        return ResponseEntity.ok(readings);
    }
    
    @GetMapping("/meta/{metaId}/timerange")
    public ResponseEntity<List<Reading>> getReadingsByMetaIdInTimeRange(
            @PathVariable String metaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date endTime) {
        List<Reading> readings = readingService.getReadingsByMetaIdInTimeRange(metaId, startTime, endTime);
        return ResponseEntity.ok(readings);
    }
    
    @GetMapping("/alarms")
    public ResponseEntity<List<Reading>> getAlarmReadings(@RequestParam(defaultValue = "true") boolean inAlarm) {
        List<Reading> readings = readingService.getAlarmReadings(inAlarm);
        return ResponseEntity.ok(readings);
    }
} 