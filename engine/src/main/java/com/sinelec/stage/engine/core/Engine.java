package com.sinelec.stage.engine.core;

import com.sinelec.stage.domain.engine.driver.Driver;
import com.sinelec.stage.domain.engine.model.*;
import com.sinelec.stage.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.HashMap;

/**
 * Single engine instance responsible for a datasource and its devices
 */
public class Engine {
    private static final Logger logger = LoggerFactory.getLogger(Engine.class);
    
    private final Datasource datasource;
    private final List<Device> devices;
    private final Driver driver;
    
    private final DeviceService deviceService;
    private final DeviceStateService deviceStateService;
    private final ReadingService readingService;
    private final DriverDefinitionService driverDefinitionService;
    
    // Engine state
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final AtomicLong lastPollTime = new AtomicLong(0);
    private final AtomicLong lastSuccessfulPollTime = new AtomicLong(0);
    private final AtomicInteger pollCount = new AtomicInteger(0);
    private final AtomicInteger errorCount = new AtomicInteger(0);
    private final AtomicInteger readingCount = new AtomicInteger(0);
    
    public Engine(
            Datasource datasource,
            List<Device> devices,
            Driver driver,
            DeviceService deviceService,
            DeviceStateService deviceStateService,
            ReadingService readingService,
            DriverDefinitionService driverDefinitionService) {
        this.datasource = datasource;
        this.devices = new ArrayList<>(devices);
        this.driver = driver;
        this.deviceService = deviceService;
        this.deviceStateService = deviceStateService;
        this.readingService = readingService;
        this.driverDefinitionService = driverDefinitionService;
    }
    
    /**
     * Start the engine
     */
    public boolean start() {
        if (running.getAndSet(true)) {
            logger.info("Engine for datasource {} is already running", datasource.getId());
            return true;
        }
        
        logger.info("Starting engine for datasource {}", datasource.getId());
        
        try {
            // Initialize the driver with datasource configuration
            driver.initialize(datasource);
            
            // Attempt to connect
            connected.set(driver.connect());
            
            if (!connected.get()) {
                logger.error("Failed to connect to datasource {}", datasource.getId());
                running.set(false);
                return false;
            }
            
            logger.info("Engine for datasource {} started successfully", datasource.getId());
            return true;
        } catch (Exception e) {
            logger.error("Error starting engine for datasource {}: {}", 
                    datasource.getId(), e.getMessage(), e);
            running.set(false);
            return false;
        }
    }
    
    /**
     * Stop the engine
     */
    public void stop() {
        if (!running.getAndSet(false)) {
            logger.info("Engine for datasource {} is already stopped", datasource.getId());
            return;
        }
        
        logger.info("Stopping engine for datasource {}", datasource.getId());
        
        try {
            driver.disconnect();
            connected.set(false);
            logger.info("Engine for datasource {} stopped successfully", datasource.getId());
        } catch (Exception e) {
            logger.error("Error stopping engine for datasource {}: {}", 
                    datasource.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * Poll data from devices
     */
    public List<Reading> poll() {
        if (!running.get() || !connected.get()) {
            logger.warn("Cannot poll - engine for datasource {} is not running/connected", 
                    datasource.getId());
            return Collections.emptyList();
        }
        
        lastPollTime.set(System.currentTimeMillis());
        pollCount.incrementAndGet();
        
        logger.debug("Polling devices for datasource {}", datasource.getId());
        
        try {
            // Create read commands for all devices
            List<DeviceCommand> readCommands = createReadCommands();
            
            // Execute the read
            List<Reading> readings = driver.read(readCommands);
            
            if (readings != null && !readings.isEmpty()) {
                // Process and save readings
                List<Reading> processedReadings = processReadings(readings);
                
                // Update statistics
                lastSuccessfulPollTime.set(System.currentTimeMillis());
                readingCount.addAndGet(processedReadings.size());
                
                logger.debug("Poll completed for datasource {} - {} readings collected", 
                        datasource.getId(), processedReadings.size());
                
                return processedReadings;
            } else {
                logger.debug("Poll completed for datasource {} - no readings returned", 
                        datasource.getId());
                return Collections.emptyList();
            }
        } catch (Exception e) {
            errorCount.incrementAndGet();
            logger.error("Error polling devices for datasource {}: {}", 
                    datasource.getId(), e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    /**
     * Write values to devices
     */
    public boolean write(DeviceCommand command) {
        if (!running.get() || !connected.get()) {
            logger.warn("Cannot write - engine for datasource {} is not running/connected", 
                    datasource.getId());
            return false;
        }
        
        try {
            // Load signal configurations and definitions
            enrichCommandWithSignalInfo(command);
            
            // Execute the write
            driver.write(List.of(command));
            
            logger.debug("Write command {} executed successfully for device {}", 
                    command.getId(), command.getDeviceId());
            return true;
        } catch (Exception e) {
            logger.error("Error writing to device {}: {}", 
                    command.getDeviceId(), e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Check if the engine is running
     */
    public boolean isRunning() {
        return running.get();
    }
    
    /**
     * Check if the engine is connected
     */
    public boolean isConnected() {
        return connected.get();
    }
    
    /**
     * Get engine statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("datasourceId", datasource.getId());
        stats.put("datasourceName", datasource.getName());
        stats.put("deviceCount", devices.size());
        stats.put("running", running.get());
        stats.put("connected", connected.get());
        stats.put("lastPollTime", lastPollTime.get() > 0 ? new Date(lastPollTime.get()) : null);
        stats.put("lastSuccessfulPollTime", lastSuccessfulPollTime.get() > 0 ? new Date(lastSuccessfulPollTime.get()) : null);
        stats.put("pollCount", pollCount.get());
        stats.put("errorCount", errorCount.get());
        stats.put("readingCount", readingCount.get());
        
        if (running.get() && connected.get()) {
            stats.put("status", "RUNNING");
        } else if (running.get()) {
            stats.put("status", "DISCONNECTED");
        } else {
            stats.put("status", "STOPPED");
        }
        
        return stats;
    }
    
    /**
     * Get the datasource ID
     */
    public String getDatasourceId() {
        return datasource.getId();
    }
    
    /**
     * Get the list of device IDs
     */
    public List<String> getDeviceIds() {
        return devices.stream()
                .map(Device::getId)
                .collect(Collectors.toList());
    }
    
    /**
     * Create read commands for all devices
     */
    private List<DeviceCommand> createReadCommands() {
        List<DeviceCommand> commands = new ArrayList<>();
        
        for (Device device : devices) {
            DeviceCommand command = DeviceCommand.builder()
                    .id(UUID.randomUUID().toString())
                    .deviceId(device.getId())
                    .datasourceId(datasource.getId())
                    .commandType(DeviceCommand.CommandType.READ)
                    .build();
            
            // Enrich with signal info
            enrichCommandWithSignalInfo(command);
            
            commands.add(command);
        }
        
        return commands;
    }
    
    /**
     * Enrich command with signal configurations and definitions
     */
    private void enrichCommandWithSignalInfo(DeviceCommand command) {
        // Find the device
        Device device = devices.stream()
                .filter(d -> d.getId().equals(command.getDeviceId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Device not found: " + command.getDeviceId()));
        
        // Set signal configurations from device
        command.setSignalConfigurations(device.getSignalConfigurations());
        
        // Load signal definitions from device definition
        List<SignalDefinition> signalDefinitions = new ArrayList<>();
        
        if (device.getDeviceDefinitionId() != null) {
            deviceService.getDeviceDefinitionById(device.getDeviceDefinitionId())
                .ifPresent(deviceDef -> {
                    if (deviceDef.getSignals() != null) {
                        for (SignalConfiguration config : device.getSignalConfigurations()) {
                            deviceDef.getSignals().stream()
                                .filter(s -> s.getId().equals(config.getSignalId()))
                                .findFirst()
                                .ifPresent(signalDefinitions::add);
                        }
                    }
                });
        }
        
        command.setSignalDefinitions(signalDefinitions);
    }
    
    /**
     * Process readings by validating values, checking for alarms, and saving to database
     */
    private List<Reading> processReadings(List<Reading> readings) {
        List<Reading> processedReadings = new ArrayList<>();
        Map<String, DeviceState> deviceStates = new HashMap<>();
        
        for (Reading reading : readings) {
            // Find device
            Device device = findDeviceById(reading.getDeviceId());
            if (device == null) continue;
            
            // Find signal definition from device definition
            Optional<SignalDefinition> signalDef = Optional.empty();
            if (device.getDeviceDefinitionId() != null) {
                Optional<DeviceDefinition> deviceDef = deviceService.getDeviceDefinitionById(device.getDeviceDefinitionId());
                if (deviceDef.isPresent() && deviceDef.get().getSignals() != null) {
                    signalDef = deviceDef.get().getSignals().stream()
                        .filter(s -> s.getId().equals(reading.getSignalId()))
                        .findFirst();
                }
            }
            
            if (signalDef.isEmpty()) continue;
            
            SignalDefinition signal = signalDef.get();
            
            // Check value validity if validation conditions are defined
            boolean valid = validateReadingValue(reading, signal);
            
            // If valid, check for alarms
            boolean inAlarm = false;
            if (valid && signal.isAlarmsEnabled()) {
                inAlarm = checkForAlarms(reading, signal);
            }
            
            reading.setInAlarm(inAlarm);
            
            // Save reading to database
            Reading savedReading = readingService.createReading(reading);
            processedReadings.add(savedReading);
            
            // Update device state
            updateDeviceState(deviceStates, device, reading, signal);
        }
        
        // Save updated device states
        for (DeviceState state : deviceStates.values()) {
            deviceStateService.updateDeviceState(state.getId(), state);
        }
        
        return processedReadings;
    }
    
    /**
     * Find a device by ID
     */
    private Device findDeviceById(String deviceId) {
        return devices.stream()
                .filter(d -> d.getId().equals(deviceId))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Validate a reading value against signal definition
     */
    private boolean validateReadingValue(Reading reading, SignalDefinition signal) {
        // Basic type validation
        if (signal.getType() != null && !signal.getType().isValidValue(reading.getValue())) {
            return false;
        }
        
        // Check validation conditions if any
        if (signal.getValidateConditions() != null) {
            for (ValidateCondition condition : signal.getValidateConditions()) {
                try {
                    Map<String, Object> variables = new HashMap<>();
                    variables.put("value", reading.getValue());
                    variables.put("numericValue", reading.getNumericValue());
                    
                    if (condition.getExpression() != null && 
                            !condition.getExpression().evaluate(variables)) {
                        return false;
                    }
                } catch (Exception e) {
                    logger.warn("Error evaluating validation condition for reading {}: {}", 
                            reading.getId(), e.getMessage());
                }
            }
        }
        
        return true;
    }
    
    /**
     * Check for alarms based on alarm conditions
     */
    private boolean checkForAlarms(Reading reading, SignalDefinition signal) {
        if (signal.getAlarmConditions() == null || signal.getAlarmConditions().isEmpty()) {
            return false;
        }
        
        for (AlarmCondition condition : signal.getAlarmConditions()) {
            try {
                Map<String, Object> variables = new HashMap<>();
                variables.put("value", reading.getValue());
                variables.put("numericValue", reading.getNumericValue());
                
                if (condition.getExpression() != null && 
                        condition.getExpression().evaluate(variables)) {
                    return true;
                }
            } catch (Exception e) {
                logger.warn("Error evaluating alarm condition for reading {}: {}", 
                        reading.getId(), e.getMessage());
            }
        }
        
        return false;
    }
    
    /**
     * Update device state with new reading
     */
    private void updateDeviceState(
            Map<String, DeviceState> deviceStates, 
            Device device, 
            Reading reading, 
            SignalDefinition signal) {
        
        // Get or create device state
        DeviceState state = deviceStates.computeIfAbsent(device.getId(), 
                id -> deviceStateService.getDeviceStateByDeviceId(id)
                .orElseGet(() -> {
                    DeviceState newState = new DeviceState();
                    newState.setDeviceId(id);
                    newState.setConnected(true);
                    newState.setConnectionStatus("Connected");
                    newState.setHealthStatus(HealthStatus.HEALTHY);
                    newState.setCreated(new Date());
                    newState = deviceStateService.createDeviceState(newState);
                    return newState;
                }));
        
        // Update the state
        state.setUpdated(new Date());
        
        // Update signal state
        boolean signalStateUpdated = false;
        
        if (state.getSignalStates() == null) {
            state.setSignalStates(new ArrayList<>());
        }
        
        for (SignalState signalState : state.getSignalStates()) {
            if (signalState.getSignalId().equals(reading.getSignalId())) {
                signalState.setLastReading(reading);
                signalStateUpdated = true;
                break;
            }
        }
        
        if (!signalStateUpdated) {
            SignalState newSignalState = new SignalState();
            newSignalState.setSignalId(reading.getSignalId());
            newSignalState.setLastReading(reading);
            state.getSignalStates().add(newSignalState);
        }
        
        // Update health status based on alarms
        if (reading.isInAlarm()) {
            // Only degrade health status if it's currently HEALTHY
            if (state.getHealthStatus() == HealthStatus.HEALTHY) {
                state.setHealthStatus(HealthStatus.DEGRADED);
            }
        }
    }
} 