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
    
    List<Reading> findByDeviceId(String deviceId);
    
    List<Reading> findByDeviceIdAndSignalId(String deviceId, String signalId);
    
    List<Reading> findByInAlarm(boolean inAlarm);
    
    @Query("{'deviceId': ?0, 'signalId': ?1, 'timestamp': {$gte: ?2, $lte: ?3}}")
    List<Reading> findReadingsByTimeRange(String deviceId, String signalId, Date startTime, Date endTime);
    
    @Query("{'deviceId': ?0, 'signalId': ?1, 'timestamp': {$gte: ?2, $lte: ?3}}")
    Page<Reading> findReadingsByTimeRangePaginated(String deviceId, String signalId, Date startTime, Date endTime, Pageable pageable);
    
    @Query("{'metaId': ?0, 'timestamp': {$gte: ?1, $lte: ?2}}")
    List<Reading> findReadingsByMetaIdAndTimeRange(String metaId, Date startTime, Date endTime);
} 