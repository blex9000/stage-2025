// Create a mock driver for tests
package com.sinelec.stage.test.integration;

import com.sinelec.stage.domain.engine.driver.Driver;
import com.sinelec.stage.domain.engine.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Mock driver implementation for testing engine functionality
 */
@Component("testMockDriver")
@Profile("test")
public class MockDriver implements Driver {
    private static final Logger logger = LoggerFactory.getLogger(MockDriver.class);
    
    private Datasource datasource;
    private boolean connected = false;
    private final Map<String, Object> lastWrittenValues = new HashMap<>();
    
    @Override
    public String getId() {
        return "MOCK_DRIVER";
    }
    
    @Override
    public DriverDefinition getDefinition() {
        DriverDefinition definition = new DriverDefinition();
        definition.setId("MOCK_DRIVER");
        definition.setName("Mock Driver");
        definition.setDescription("Mock driver for testing");
        definition.setVersion("1.0.0");
        definition.setTags(List.of("test", "mock"));
        return definition;
    }
    
    @Override
    public void initialize(Datasource datasource) {
        this.datasource = datasource;
        logger.info("MockDriver initialized with datasource: {}", datasource.getId());
    }
    
    @Override
    public boolean connect() {
        connected = true;
        logger.info("MockDriver connected to datasource: {}", datasource.getId());
        return true;
    }
    
    @Override
    public void disconnect() {
        connected = false;
        logger.info("MockDriver disconnected from datasource: {}", datasource.getId());
    }
    
    @Override
    public boolean isConnected() {
        return connected;
    }
    
    @Override
    public List<Reading> execute(List<DeviceCommand> commands) {
        if (!connected) {
            logger.warn("MockDriver not connected, cannot read");
            return Collections.emptyList();
        }
        
        List<Reading> readings = new ArrayList<>();
        
        for (DeviceCommand command : commands) {
            String deviceId = command.getDeviceId();
            
            // Handle read requests based on command.read list
            if (command.getCommandType() == DeviceCommand.CommandType.READ && command.getRead() != null && !command.getRead().isEmpty()) {
                for (DeviceCommand.Read readRequest : command.getRead()) {
                    String signalId = readRequest.getSignalId();
                    
                    // Try to find signal definition for this signalId
                    SignalDefinition signalDef = findSignalDefinition(command, signalId);
                    if (signalDef != null) {
                        readings.add(createMockReading(deviceId, signalDef));
                        continue;
                    }
                    
                    // If no signal definition found, try to find signal configuration
                    SignalConfiguration signalConfig = findSignalConfiguration(command, signalId);
                    if (signalConfig != null) {
                        readings.add(createMockReadingFromConfig(deviceId, signalConfig));
                        continue;
                    }
                    
                    // If neither found, create a basic reading with the signalId
                    readings.add(createMockReadingFromId(deviceId, signalId));
                }
            }
            // For backwards compatibility, also handle commands without explicit read list
            else if (command.getSignalDefinitions() != null && !command.getSignalDefinitions().isEmpty()) {
                for (SignalDefinition signalDef : command.getSignalDefinitions()) {
                    readings.add(createMockReading(deviceId, signalDef));
                }
            }
            else if (command.getSignalConfigurations() != null && !command.getSignalConfigurations().isEmpty()) {
                for (SignalConfiguration signalConfig : command.getSignalConfigurations()) {
                    readings.add(createMockReadingFromConfig(deviceId, signalConfig));
                }
            }
        }
        
        logger.info("MockDriver read {} readings from {} commands", readings.size(), commands.size());
        return readings;
    }
    
    @Override
    public void write(List<DeviceCommand> commands) {
        if (!connected) {
            logger.warn("MockDriver not connected, cannot write");
            return;
        }
        
        for (DeviceCommand command : commands) {
            if (command.getCommandType() == DeviceCommand.CommandType.WRITE && command.getWrite() != null && !command.getWrite().isEmpty()) {
                for (DeviceCommand.Write writeValue : command.getWrite()) {
                    // Store last written value for verification
                    lastWrittenValues.put(writeValue.getSignalId(), writeValue.getValue());
                    logger.info("MockDriver wrote value '{}' to signal {}", 
                            writeValue.getValue(), writeValue.getSignalId());
                }
            }
        }
    }
    
    /**
     * Create a mock reading from a signal definition
     */
    private Reading createMockReading(String deviceId, SignalDefinition signalDef) {
        Reading reading = new Reading();
        reading.setId(UUID.randomUUID().toString());
        reading.setDatasourceId(datasource.getId());
        reading.setDeviceId(deviceId);
        reading.setSignalId(signalDef.getId());
        reading.setTimestamp(new Date());
        
        // Generate mock value based on datatype
        if (signalDef.getType() == DataType.BOOLEAN) {
            reading.setValue(String.valueOf(ThreadLocalRandom.current().nextBoolean()));
        } else if (signalDef.getType() == DataType.INTEGER) {
            reading.setValue(String.valueOf(ThreadLocalRandom.current().nextInt(0, 100)));
        } else if (signalDef.getType() == DataType.FLOAT) {
            // Use previously written value if available, otherwise generate random
            if (lastWrittenValues.containsKey(signalDef.getId())) {
                reading.setValue(lastWrittenValues.get(signalDef.getId()).toString());
            } else {
                reading.setValue(String.format("%.2f", ThreadLocalRandom.current().nextDouble(0, 100)));
            }
        } else {
            reading.setValue("MockValue-" + signalDef.getId());
        }
        
        // Set quality and alarm attributes
        reading.setInAlarm(false);
        reading.setMetaId(deviceId + ":" + signalDef.getId());
        
        return reading;
    }
    
    /**
     * Create a mock reading from a signal configuration
     */
    private Reading createMockReadingFromConfig(String deviceId, SignalConfiguration signalConfig) {
        Reading reading = new Reading();
        reading.setId(UUID.randomUUID().toString());
        reading.setDatasourceId(datasource.getId());
        reading.setDeviceId(deviceId);
        reading.setSignalId(signalConfig.getSignalId());
        reading.setTimestamp(new Date());
        
        // Use previously written value if available, otherwise generate random value
        if (lastWrittenValues.containsKey(signalConfig.getSignalId())) {
            reading.setValue(lastWrittenValues.get(signalConfig.getSignalId()).toString());
        } else {
            if (signalConfig.getSignalId().contains("temp")) {
                reading.setValue(String.format("%.1f", ThreadLocalRandom.current().nextDouble(15, 35)));
            } else if (signalConfig.getSignalId().contains("status")) {
                reading.setValue(String.valueOf(ThreadLocalRandom.current().nextBoolean()));
            } else {
                reading.setValue(String.valueOf(ThreadLocalRandom.current().nextInt(0, 100)));
            }
        }
        
        reading.setInAlarm(false);
        reading.setMetaId(deviceId + ":" + signalConfig.getSignalId());
        
        return reading;
    }
    
    /**
     * Create a mock reading from just a signal ID
     */
    private Reading createMockReadingFromId(String deviceId, String signalId) {
        Reading reading = new Reading();
        reading.setId(UUID.randomUUID().toString());
        reading.setDatasourceId(datasource.getId());
        reading.setDeviceId(deviceId);
        reading.setSignalId(signalId);
        reading.setTimestamp(new Date());
        
        // Use previously written value if available, otherwise generate random value
        if (lastWrittenValues.containsKey(signalId)) {
            reading.setValue(lastWrittenValues.get(signalId).toString());
        } else {
            if (signalId.contains("temp")) {
                reading.setValue(String.format("%.1f", ThreadLocalRandom.current().nextDouble(15, 35)));
            } else if (signalId.contains("status")) {
                reading.setValue(String.valueOf(ThreadLocalRandom.current().nextBoolean()));
            } else {
                reading.setValue(String.valueOf(ThreadLocalRandom.current().nextInt(0, 100)));
            }
        }
        
        reading.setInAlarm(false);
        reading.setMetaId(deviceId + ":" + signalId);
        
        return reading;
    }
} 