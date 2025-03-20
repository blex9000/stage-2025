package com.sinelec.stage.repository;

import com.sinelec.stage.domain.engine.model.Reading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ReadingRepository extends MongoRepository<Reading, String> {
    
    /**
     * Find all readings for a specific device
     */
    List<Reading> findByDeviceId(String deviceId);
    
    /**
     * Find readings by device ID and signal ID
     */
    List<Reading> findByDeviceIdAndSignalId(String deviceId, String signalId);
    
    /**
     * Find readings by alarm status
     */
    List<Reading> findByInAlarm(boolean inAlarm);
    
    /**
     * Find readings in a time range for a device and signal
     */
    @Query("{'deviceId': ?0, 'signalId': ?1, 'timestamp': {$gte: ?2, $lte: ?3}}")
    List<Reading> findReadingsByTimeRange(String deviceId, String signalId, Date startTime, Date endTime);
    
    /**
     * Find paginated readings in a time range for a device and signal
     */
    @Query("{'deviceId': ?0, 'signalId': ?1, 'timestamp': {$gte: ?2, $lte: ?3}}")
    Page<Reading> findReadingsByTimeRangePaginated(String deviceId, String signalId, Date startTime, Date endTime, Pageable pageable);
    
    /**
     * Find readings in a time range by meta ID (deviceId:signalId)
     */
    @Query("{'metaId': ?0, 'timestamp': {$gte: ?1, $lte: ?2}}")
    List<Reading> findReadingsByMetaIdAndTimeRange(String metaId, Date startTime, Date endTime);
} 