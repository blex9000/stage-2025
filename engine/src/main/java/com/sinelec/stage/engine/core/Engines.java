package com.sinelec.stage.engine.core;

import com.sinelec.stage.domain.engine.model.*;
import com.sinelec.stage.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;  
import jakarta.annotation.PreDestroy;
import com.sinelec.stage.domain.engine.driver.Driver;

/**
 * Manages all engine instances
 */
@Component
public class Engines {
    private static final Logger logger = LoggerFactory.getLogger(Engines.class);
    
    private final Map<String, Engine> engines = new ConcurrentHashMap<>();
    
    private final DriverRegistry driverRegistry;
    private final DatasourceService datasourceService;
    private final DeviceService deviceService;
    private final DeviceDefinitionService deviceDefinitionService;
    private final DeviceStateService deviceStateService;
    private final ReadingService readingService;
    
    @Autowired
    public Engines(
            DriverRegistry driverRegistry,
            DatasourceService datasourceService,
            DeviceService deviceService,
            DeviceDefinitionService deviceDefinitionService, DeviceStateService deviceStateService,
            ReadingService readingService) {
        this.driverRegistry = driverRegistry;
        this.datasourceService = datasourceService;
        this.deviceService = deviceService;
        this.deviceDefinitionService = deviceDefinitionService;
        this.deviceStateService = deviceStateService;
        this.readingService = readingService;
    }
    
    @PostConstruct
    public void initialize() {
        logger.info("Initializing Engines manager");
        // Load active datasources
        loadAndStartEngines();
    }
    
    @PreDestroy
    public void shutdown() {
        logger.info("Shutting down all engines");
        
        // Stop all engines
        for (Engine engine : engines.values()) {
            try {
                engine.stop();
            } catch (Exception e) {
                logger.error("Error stopping engine for datasource {}: {}", 
                        engine.getDatasourceId(), e.getMessage(), e);
            }
        }
        
        engines.clear();
    }
    
    /**
     * Load and start engines for all active datasources
     */
    public void loadAndStartEngines() {
        logger.info("Loading engines for active datasources");
        
        // Get all active datasources
        List<Datasource> activeDatasources = datasourceService.getActiveDatasources();
        
        logger.info("Found {} active datasources", activeDatasources.size());
        
        for (Datasource datasource : activeDatasources) {
            try {
                createAndStartEngine(datasource);
            } catch (Exception e) {
                logger.error("Error creating engine for datasource {}: {}", 
                        datasource.getId(), e.getMessage(), e);
            }
        }
    }
    
    /**
     * Create and start an engine for a datasource
     */
    public boolean createAndStartEngine(Datasource datasource) {
        return createAndStartEngine(datasource.getId());
    }
    
    /**
     * Create and start an engine for a datasource ID
     */
    public boolean createAndStartEngine(String datasourceId) {
        if (engines.containsKey(datasourceId)) {
            logger.info("Engine for datasource {} already exists", datasourceId);
            return true;
        }
        
        logger.info("Creating engine for datasource {}", datasourceId);
        
        try {
            // Create engine instance
            Engine engine = new Engine(
                    datasourceId,
                    driverRegistry,
                    datasourceService,
                    deviceService,
                    deviceDefinitionService,
                    deviceStateService,
                    readingService);
            
            // Start the engine
            boolean started = engine.start();
            
            if (started) {
                // Store in engines map
                engines.put(datasourceId, engine);
                logger.info("Engine for datasource {} started successfully", datasourceId);
            } else {
                logger.error("Failed to start engine for datasource {}", datasourceId);
            }
            
            return started;
        } catch (Exception e) {
            logger.error("Error creating engine for datasource {}: {}", 
                    datasourceId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Stop and remove an engine
     */
    public boolean stopEngine(String datasourceId) {
        Engine engine = engines.get(datasourceId);
        
        if (engine == null) {
            logger.warn("Engine for datasource {} not found", datasourceId);
            return false;
        }
        
        logger.info("Stopping engine for datasource {}", datasourceId);
        
        try {
            engine.stop();
            engines.remove(datasourceId);
            logger.info("Engine for datasource {} stopped and removed", datasourceId);
            return true;
        } catch (Exception e) {
            logger.error("Error stopping engine for datasource {}: {}", 
                    datasourceId, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Get all active engine IDs
     */
    public List<String> getActiveEngineIds() {
        return new ArrayList<>(engines.keySet());
    }
    
    /**
     * Get engine statistics for all engines
     */
    public List<Map<String, Object>> getAllEngineStatistics() {
        return engines.values().stream()
                .map(Engine::getStatistics)
                .collect(Collectors.toList());
    }
    
    /**
     * Get engine statistics for a specific engine
     */
    public Map<String, Object> getEngineStatistics(String datasourceId) {
        Engine engine = engines.get(datasourceId);
        
        if (engine == null) {
            return Collections.emptyMap();
        }
        
        return engine.getStatistics();
    }
    
    /**
     * Poll all engines for readings
     */
    @Scheduled(fixedRateString = "${app.engine.poll.interval:10000}")
    public void pollAllEngines() {
        logger.debug("Polling all engines: {}", engines.size());
        
        for (Engine engine : engines.values()) {
            try {
                if (engine.isRunning() && engine.isConnected()) {
                    engine.poll();
                }
            } catch (Exception e) {
                logger.error("Error polling engine for datasource {}: {}", 
                        engine.getDatasourceId(), e.getMessage(), e);
            }
        }
    }
    
    /**
     * Poll a specific engine by datasource ID
     */
    public List<Reading> pollEngine(String datasourceId) {
        Engine engine = engines.get(datasourceId);
        
        if (engine == null) {
            logger.warn("Engine for datasource {} not found", datasourceId);
            return Collections.emptyList();
        }
        
        if (!engine.isRunning() || !engine.isConnected()) {
            logger.warn("Engine for datasource {} is not running/connected", datasourceId);
            return Collections.emptyList();
        }
        
        return engine.poll();
    }
    
    /**
     * Poll engines by device ID
     */
    public List<Reading> pollDevices(List<String> deviceIds) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            logger.warn("No device IDs provided for polling");
            return Collections.emptyList();
        }
        
        logger.debug("Polling for devices: {}", deviceIds);
        
        Map<String, List<String>> datasourceDevices = new HashMap<>();
        
        // Group device IDs by datasource
        for (String deviceId : deviceIds) {
            deviceService.getDeviceById(deviceId).ifPresent(device -> {
                String datasourceId = device.getDatasourceId();
                if (datasourceId != null) {
                    datasourceDevices.computeIfAbsent(datasourceId, k -> new ArrayList<>())
                        .add(deviceId);
                }
            });
        }
        
        // Poll each datasource engine for its devices
        List<Reading> allReadings = new ArrayList<>();
        
        for (Map.Entry<String, List<String>> entry : datasourceDevices.entrySet()) {
            String datasourceId = entry.getKey();
            List<String> dsDeviceIds = entry.getValue();
            
            Engine engine = engines.get(datasourceId);
            if (engine != null && engine.isRunning() && engine.isConnected()) {
                List<Reading> readings = engine.poll();
                
                // Filter by requested device IDs
                List<Reading> filteredReadings = readings.stream()
                    .filter(r -> dsDeviceIds.contains(r.getDeviceId()))
                    .collect(Collectors.toList());
                
                allReadings.addAll(filteredReadings);
            }
        }
        
        return allReadings;
    }
    
    
    /**
     * Execute a command to a device
     */
    public boolean executeCommand(DeviceCommand command) {
        if (command == null || command.getDeviceId() == null || command.getDatasourceId() == null) {
            logger.warn("Invalid command provided");
            return false;
        }
        
        String datasourceId = command.getDatasourceId();
        Engine engine = engines.get(datasourceId);
        
        if (engine == null) {
            logger.warn("Engine for datasource {} not found", datasourceId);
            return false;
        }
        
        if (!engine.isRunning() || !engine.isConnected()) {
            logger.warn("Engine for datasource {} is not running/connected", datasourceId);
            return false;
        }
        
        return engine.executeCommand(command);
    }
    
    /**
     * Get the status of all engines (active/inactive)
     */
    public Map<String, Boolean> getAllEngineStatus() {
        Map<String, Boolean> status = new HashMap<>();
        
        for (String datasourceId : engines.keySet()) {
            Engine engine = engines.get(datasourceId);
            status.put(datasourceId, engine.isRunning() && engine.isConnected());
        }
        
        return status;
    }
    
    /**
     * Restart a specific engine 
     */
    public boolean restartEngine(String datasourceId) {
        // Stop the engine first
        boolean stopped = stopEngine(datasourceId);
        
        if (!stopped) {
            logger.warn("Failed to stop engine for datasource {}", datasourceId);
            return false;
        }
        
        // Get the datasource
        Optional<Datasource> datasource = datasourceService.getDatasourceById(datasourceId);
        if (datasource.isEmpty()) {
            logger.error("Datasource {} not found for restarting", datasourceId);
            return false;
        }
        
        // Start the engine with the datasource
        return createAndStartEngine(datasource.get());
    }
} 