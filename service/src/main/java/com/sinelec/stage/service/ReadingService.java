package com.sinelec.stage.service;

import com.sinelec.stage.domain.engine.model.Reading;
import com.sinelec.stage.repository.ReadingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReadingService {
    
    private final ReadingRepository readingRepository;
    
    @Autowired
    public ReadingService(ReadingRepository readingRepository) {
        this.readingRepository = readingRepository;
    }
    
    public Reading saveReading(Reading reading) {
        // Set metaId if not already set
        if (reading.getMetaId() == null && reading.getDeviceId() != null && reading.getSignalId() != null) {
            reading.setMetaId(reading.getDeviceId() + ":" + reading.getSignalId());
        }
        return readingRepository.save(reading);
    }
    
    public List<Reading> saveReadings(List<Reading> readings) {
        return readingRepository.saveAll(readings);
    }
    
    public Optional<Reading> getReadingById(String id) {
        return readingRepository.findById(id);
    }
    
    public List<Reading> getReadingsByDeviceAndSignal(String deviceId, String signalId) {
        return readingRepository.findByDeviceIdAndSignalId(deviceId, signalId);
    }
    
    public List<Reading> getReadingsByDeviceAndSignalInTimeRange(
            String deviceId, String signalId, Date startTime, Date endTime) {
        return readingRepository.findReadingsByTimeRange(deviceId, signalId, startTime, endTime);
    }
    
    public Page<Reading> getReadingsByDeviceAndSignalInTimeRangePaginated(
            String deviceId, String signalId, Date startTime, Date endTime, Pageable pageable) {
        return readingRepository.findReadingsByTimeRangePaginated(deviceId, signalId, startTime, endTime, pageable);
    }
    
    public List<Reading> getReadingsByMetaIdInTimeRange(String metaId, Date startTime, Date endTime) {
        return readingRepository.findReadingsByMetaIdAndTimeRange(metaId, startTime, endTime);
    }
    
    public List<Reading> getAlarmReadings(boolean inAlarm) {
        return readingRepository.findByInAlarm(inAlarm);
    }
    
    /**
     * Create a new reading
     */
    public Reading createReading(Reading reading) {
        // Set created timestamp if not set
        if (reading.getTimestamp() == null) {
            reading.setTimestamp(new Date());
        }
        
        // Generate ID if not present
        if (reading.getId() == null || reading.getId().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }
        
        return readingRepository.save(reading);
    }
} 