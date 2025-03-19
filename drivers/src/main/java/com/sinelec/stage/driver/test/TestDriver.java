package com.sinelec.stage.driver.test;

import com.sinelec.stage.domain.engine.driver.Driver;
import com.sinelec.stage.domain.engine.model.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Component
@Scope("prototype")
public class TestDriver implements Driver {
    private static final String DRIVER_ID = "TEST_DRIVER";
    private final Random random = new Random();
    private Datasource datasource;
    private boolean connected = false;
    
    @Override
    public String getId() {
        return DRIVER_ID;
    }
    
    @Override
    public DriverDefinition getDefinition() {
        DriverDefinition definition = new DriverDefinition();
        definition.setId(DRIVER_ID);
        definition.setName("Test Driver");
        definition.setDescription("Test driver for simulation purposes");
        definition.setVersion("1.0.0");
        definition.setTags(List.of("random","test"));
        
        // Define configuration properties
        List<PropertyDefinition> configProps = new ArrayList<>();
        
        // Add a "delay" property
        configProps.add(PropertyDefinition.builder()
            .name("DELAY")
            .description("Simulated communication delay in milliseconds")
            .required(false)
            .defaultValue("0")
            .valueType(DataType.INTEGER)
            .build());
            
        // Add a "failure_rate" property
        configProps.add(PropertyDefinition.builder()
            .name("FAILURE_RATE")
            .description("Percentage chance of simulated communication failure (0-100)")
            .required(false)
            .defaultValue("0")
            .valueType(DataType.INTEGER)
            .build());
            
        // Add a "value_range" property
        configProps.add(PropertyDefinition.builder()
            .name("VALUE_RANGE")
            .description("Range for random numeric values (min,max)")
            .required(false)
            .defaultValue("0,100")
            .valueType(DataType.STRING)
            .build());
            
        definition.setConfigurationProperties(configProps);
        
        return definition;
    }
    
    @Override
    public void initialize(Datasource datasource) {
        this.datasource = datasource;
    }
    
    @Override
    public boolean connect() {
        // Simulate connection delay
        simulateDelay();
        
        // Check for simulated failure
        if (shouldFail()) {
            connected = false;
            return false;
        }
        
        connected = true;
        return true;
    }
    
    @Override
    public void disconnect() {
        connected = false;
    }
    
    @Override
    public boolean isConnected() {
        return connected;
    }
    
    @Override
    public List<Reading> read(List<DeviceCommand> commands) throws Exception {
        if (!connected) {
            throw new IllegalStateException("Driver not connected");
        }
        
        // Simulate read delay
        simulateDelay();
        
        // Check for simulated failure
        if (shouldFail()) {
            throw new Exception("Simulated communication failure");
        }
        
        List<Reading> readings = new ArrayList<>();
        
        // Generate random readings for each command/signal
        for (DeviceCommand command : commands) {
            String deviceId = command.getDeviceId();
            
            // For each signal, create a reading with random value
            for (SignalDefinition signal : command.getSignalDefinitions()) {
                Reading reading = new Reading();
                reading.setDeviceId(deviceId);
                reading.setSignalId(signal.getId());
                reading.setTimestamp(new Date());
                
                // Generate appropriate random value based on signal type
                Object value = generateRandomValue(signal.getType());
                reading.setValue(value);
                
                // Convert to numeric if applicable
                if (value instanceof Number) {
                    reading.setNumericValue(((Number) value).doubleValue());
                }
                
                readings.add(reading);
            }
        }
        
        return readings;
    }
    
    @Override
    public void write(List<DeviceCommand> commands) throws Exception {
        if (!connected) {
            throw new IllegalStateException("Driver not connected");
        }
        
        // Simulate write delay
        simulateDelay();
        
        // Check for simulated failure
        if (shouldFail()) {
            throw new Exception("Simulated communication failure");
        }
        
        // Just simulate a successful write - no actual action needed
    }
    
    private void simulateDelay() {
        int delay = getDelayMs();
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    private boolean shouldFail() {
        int failureRate = getFailureRate();
        return failureRate > 0 && random.nextInt(100) < failureRate;
    }
    
    private int getDelayMs() {
        if (datasource == null || datasource.getConfiguration() == null) {
            return 0;
        }
        
        return datasource.getConfiguration().stream()
            .filter(p -> "DELAY".equals(p.getName()))
            .findFirst()
            .map(p -> {
                try {
                    return Integer.parseInt(p.getValue());
                } catch (NumberFormatException e) {
                    return 0;
                }
            })
            .orElse(0);
    }
    
    private int getFailureRate() {
        if (datasource == null || datasource.getConfiguration() == null) {
            return 0;
        }
        
        return datasource.getConfiguration().stream()
            .filter(p -> "FAILURE_RATE".equals(p.getName()))
            .findFirst()
            .map(p -> {
                try {
                    return Integer.parseInt(p.getValue());
                } catch (NumberFormatException e) {
                    return 0;
                }
            })
            .orElse(0);
    }
    
    private Object generateRandomValue(DataType type) {
        switch (type) {
            case BOOLEAN:
                return random.nextBoolean();
            case INTEGER:
                return random.nextInt(getValueRange()[1] - getValueRange()[0]) + getValueRange()[0];
            case FLOAT:
            case DOUBLE:
                return random.nextDouble() * (getValueRange()[1] - getValueRange()[0]) + getValueRange()[0];
            case STRING:
                return "Value-" + random.nextInt(1000);
            default:
                return random.nextDouble() * 100;
        }
    }
    
    private int[] getValueRange() {
        if (datasource == null || datasource.getConfiguration() == null) {
            return new int[]{0, 100};
        }
        
        return datasource.getConfiguration().stream()
            .filter(p -> "VALUE_RANGE".equals(p.getName()))
            .findFirst()
            .map(p -> {
                try {
                    String[] parts = p.getValue().split(",");
                    return new int[]{
                        Integer.parseInt(parts[0].trim()),
                        Integer.parseInt(parts[1].trim())
                    };
                } catch (Exception e) {
                    return new int[]{0, 100};
                }
            })
            .orElse(new int[]{0, 100});
    }
} 