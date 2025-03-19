// Create a mock driver for tests
package com.sinelec.stage.engine.integration;

import com.sinelec.stage.domain.engine.driver.Driver;
import com.sinelec.stage.domain.engine.model.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;

@Component("testMockDriver")
@Profile("test")
public class MockDriver implements Driver {
    private Datasource datasource;
    private boolean connected = false;
    
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
    }
    
    @Override
    public boolean connect() {
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
        List<Reading> readings = new ArrayList<>();
        
        for (DeviceCommand command : commands) {
            String deviceId = command.getDeviceId();
            
            // Create mock readings for each signal
            if (command.getSignalDefinitions() != null) {
                for (SignalDefinition signal : command.getSignalDefinitions()) {
                    Reading reading = new Reading();
                    reading.setId(UUID.randomUUID().toString());
                    reading.setDeviceId(deviceId);
                    reading.setSignalId(signal.getId());
                    reading.setTimestamp(new Date());
                    
                    // Generate mock value based on signal type
                    if (signal.getType() == DataType.BOOLEAN) {
                        reading.setValue(true);
                    } else if (signal.getType() == DataType.INTEGER) {
                        reading.setValue(42);
                        reading.setNumericValue(42.0);
                    } else if (signal.getType() == DataType.FLOAT) {
                        reading.setValue(42.5);
                        reading.setNumericValue(42.5);
                    } else {
                        reading.setValue("Test value");
                    }
                    
                    readings.add(reading);
                }
            }
        }
        
        return readings;
    }
    
    @Override
    public void write(List<DeviceCommand> commands) throws Exception {
        // Mock implementation, do nothing
    }
} 