package com.sinelec.stage.drivers.test;

import com.sinelec.stage.domain.engine.driver.Driver;
import com.sinelec.stage.domain.engine.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * A test driver implementation
 */
@Component("test-driver-v1")
public class TestDriver implements Driver {
    private static final Logger logger = LoggerFactory.getLogger(TestDriver.class);
    private static final String DRIVER_ID = "test-driver-v1";
    private static final String DRIVER_NAME = "Test Driver v1";
    private static final String DRIVER_DESCRIPTION = "Simulation driver for testing purposes";
    
    // Property keys
    private static final String MIN_DELAY_KEY = "minConnectionDelay";
    private static final String MAX_DELAY_KEY = "maxConnectionDelay";
    private static final String DEFAULT_MIN_DELAY = "100";
    private static final String DEFAULT_MAX_DELAY = "1000";
    
    private final Random random = new Random();
    private boolean connected = false;
    private Datasource datasource;
    private Map<String, Object> lastWrittenValues = new HashMap<>();

    @Override
    public String getId() {
        return DRIVER_ID;
    }
    
    @Override
    public DriverDefinition getDefinition() {
        // Configuration properties (for connection parameters)
        List<PropertyDefinition> configProps = new ArrayList<>();
        
        configProps.add(PropertyDefinition.builder()
                .name(MIN_DELAY_KEY)
                .description("Minimum delay in milliseconds when simulating connection")
                .valueType(DataType.INTEGER)
                .required(false)
                .defaultValue(DEFAULT_MIN_DELAY)
                .build());
                
        configProps.add(PropertyDefinition.builder()
                .name(MAX_DELAY_KEY)
                .description("Maximum delay in milliseconds when simulating connection")
                .valueType(DataType.INTEGER)
                .required(false)
                .defaultValue(DEFAULT_MAX_DELAY)
                .build());
        
        // Signal properties (define available signal configuration options)
        List<PropertyDefinition> signalProps = new ArrayList<>();
        
        signalProps.add(PropertyDefinition.builder()
                .name("minValue")
                .description("Minimum value for random signal generation")
                .valueType(DataType.FLOAT)
                .required(false)
                .defaultValue("0")
                .build());
                
        signalProps.add(PropertyDefinition.builder()
                .name("maxValue")
                .description("Maximum value for random signal generation")
                .valueType(DataType.FLOAT)
                .required(false)
                .defaultValue("100")
                .build());
                
        signalProps.add(PropertyDefinition.builder()
                .name("valueType")
                .description("Type of value to generate (RANDOM or STATIC)")
                .valueType(DataType.STRING)
                .required(false)
                .defaultValue("RANDOM")
                .allowedValues(Map.of(
                    "RANDOM", "Random value within range",
                    "STATIC", "Static predefined value"
                ))
                .build());
        
        return DriverDefinition.builder()
                .id(DRIVER_ID)
                .name(DRIVER_NAME)
                .description(DRIVER_DESCRIPTION)
                .version("1.0.0")
                .connectionProperties(configProps)
                .signalProperties(signalProps)
                .tags(List.of("test", "simulation"))
                .build();
    }
    
    @Override
    public void initialize(Datasource datasource) {
        this.datasource = datasource;
        logger.info("TestDriver initialized with datasource: {}", datasource.getId());
        
        // Store this datasource ID to use when generating readings
        if (datasource != null) {
            logger.info("TestDriver initialized with datasource ID: {}", datasource.getId());
        }
    }
    
    @Override
    public boolean connect() {
        if (connected) {
            return true;
        }
        
        // Simulate connection delay
        int delayMs = calculateConnectionDelay();
        try {
            logger.info("TestDriver connecting to datasource with delay {} ms", delayMs);
            TimeUnit.MILLISECONDS.sleep(delayMs);
            connected = true;
            logger.info("TestDriver connected successfully to datasource: {}", datasource.getId());
        } catch (InterruptedException e) {
            logger.error("Connection interrupted", e);
            Thread.currentThread().interrupt();
            return false;
        }
        
        return connected;
    }
    
    @Override
    public void disconnect() {
        connected = false;
        logger.info("TestDriver disconnected from datasource: {}", datasource.getId());
    }
    
    @Override
    public boolean isConnected() {
        return connected;
    }
    
    @Override
    public List<Reading> execute(List<DeviceCommand> commands) throws Exception {
        if (!connected) {
            logger.warn("TestDriver not connected, cannot read");
            throw new IllegalStateException("Driver is not connected");
        }
        
        List<Reading> readings = new ArrayList<>();
        
        for (DeviceCommand command : commands) {
            String deviceId = command.getDeviceId();
            
            // Only process READ commands
            if (command.getCommandType() == DeviceCommand.CommandType.READ) {
                // Process the read list
                if (command.getRead() != null && !command.getRead().isEmpty()) {
                    for (DeviceCommand.Read readRequest : command.getRead()) {
                        String signalId = readRequest.getSignalId();
                        
                        // Find configuration for this signal ID
                        SignalDefinition signalDef = findSignalDefinition(command, signalId);
                        SignalConfiguration signalConfig = findSignalConfiguration(command, signalId);
                        
                        readings.add(generateReading(
                                command.getDatasourceId(), 
                                deviceId, 
                                signalId, 
                                signalDef, 
                                signalConfig));
                    }
                }
                // If no read list, use all signal definitions and configurations
                else {
                    // Process signal definitions if available
                    if (command.getSignalDefinitions() != null) {
                        for (SignalDefinition signalDef : command.getSignalDefinitions()) {
                            readings.add(generateReading(
                                    datasource.getId(), 
                                    deviceId, 
                                    signalDef.getId(), 
                                    signalDef, 
                                    null));
                        }
                    }
                    
                    // Process signal configurations if available
                    if (command.getSignalConfigurations() != null) {
                        for (SignalConfiguration signalConfig : command.getSignalConfigurations()) {
                            // Find the matching signal definition if available
                            SignalDefinition signalDef = findSignalDefinition(command, signalConfig.getSignalId());
                            
                            readings.add(generateReading(
                                    datasource.getId(), 
                                    deviceId, 
                                    signalConfig.getSignalId(), 
                                    signalDef, 
                                    signalConfig));
                        }
                    }
                }
            }
        }
        
        logger.info("TestDriver read {} readings from {} commands", readings.size(), commands.size());
        return readings;
    }
    
    @Override
    public void write(List<DeviceCommand> commands) throws Exception {
        if (!connected) {
            logger.warn("TestDriver not connected, cannot write");
            throw new IllegalStateException("Driver is not connected");
        }
        
        for (DeviceCommand command : commands) {
            if (command.getCommandType() == DeviceCommand.CommandType.WRITE && command.getWrite() != null && !command.getWrite().isEmpty()) {
                for (DeviceCommand.Write writeValue : command.getWrite()) {
                    String signalId = writeValue.getSignalId();
                    String value = writeValue.getValue();
                    
                    // Store the written value for future reads
                    lastWrittenValues.put(signalId, value);
                    
                    logger.info("TestDriver wrote value '{}' to signal {}", value, signalId);
                }
            }
        }
    }
    
    /**
     * Generate a reading for the given device and signal
     */
    private Reading generateReading(String datasourceId, String deviceId, String signalId, 
                                   SignalDefinition signalDef, SignalConfiguration signalConfig) {
        
        // Check if this signal has a value that was previously written
        if (lastWrittenValues.containsKey(signalId)) {
            Object value = lastWrittenValues.get(signalId);
            logger.debug("Using previously written value for signal {}: {}", signalId, value);
            
            // Create and return reading with the previously written value
            Reading reading = Reading.builder()
                    .id(UUID.randomUUID().toString())
                    .datasourceId(datasourceId)
                    .deviceId(deviceId)
                    .signalId(signalId)
                    .value(value)
                    .timestamp(new Date())
                    .valid(true)
                    .build();
            
            return reading;
        }
        
        // Generate a new value based on signal definition and configuration
        Object value;
        
        if (signalDef != null) {
            // Generate based on data type from signal definition
            value = generateValueByType(signalDef.getType());
        } else {
            // Default to random integer if no configuration
            value = random.nextInt(100);
        }
        
        // Build and return the reading
        Reading reading = Reading.builder()
                .id(UUID.randomUUID().toString())
                .datasourceId(datasourceId)
                .deviceId(deviceId)
                .signalId(signalId)
                .value(value)
                .timestamp(new Date())
                .valid(true)
                .build();
        
        return reading;
    }
    
    /**
     * Generate a random value based on data type
     */
    private Object generateValueByType(DataType type) {
        if (type == null) {
            return random.nextInt(100); // Default
        }
        
        switch (type) {
            case BOOLEAN:
                return random.nextBoolean();
            case INTEGER:
                return random.nextInt(100);
            case FLOAT:
                return random.nextDouble() * 100.0;
            case STRING:
                return "Value-" + UUID.randomUUID().toString().substring(0, 8);
            default:
                return random.nextInt(100);
        }
    }

    /**
     * Calculate a random connection delay based on configured min/max values
     */
    private int calculateConnectionDelay() {
        int minDelay = getIntProperty(MIN_DELAY_KEY, Integer.parseInt(DEFAULT_MIN_DELAY));
        int maxDelay = getIntProperty(MAX_DELAY_KEY, Integer.parseInt(DEFAULT_MAX_DELAY));
        
        if (minDelay >= maxDelay) {
            return minDelay;
        }
        
        return minDelay + random.nextInt(maxDelay - minDelay + 1);
    }
    
    /**
     * Helper method to get integer property values from datasource
     */
    private int getIntProperty(String key, int defaultValue) {
        if (datasource == null || datasource.getConfiguration() == null) {
            return defaultValue;
        }
        
        // Find property by name in the configuration list
        Optional<Property> prop = datasource.getConfiguration().stream()
                .filter(p -> key.equals(p.getName()))
                .findFirst();
                
        if (!prop.isPresent() || prop.get().getValue() == null) {
            return defaultValue;
        }
        
        Object value = prop.get().getValue();
        
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                logger.warn("Invalid number format for property {}: {}", key, value);
                return defaultValue;
            }
        }
        
        return defaultValue;
    }
    
    /**
     * Get the last written value for a specific signal
     */
    public Object getLastWrittenValue(String signalId) {
        return lastWrittenValues.get(signalId);
    }
} 