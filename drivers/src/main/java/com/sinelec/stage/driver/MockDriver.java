package com.sinelec.stage.driver;

import com.sinelec.stage.domain.engine.driver.Driver;
import com.sinelec.stage.domain.engine.model.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope("prototype")
public class MockDriver implements Driver {
    private Datasource datasource;
    private boolean connected = false;
    
    @Override
    public String getId() {
        return "MOCK_DRIVER";
    }
    
    @Override
    public DriverDefinition getDefinition() {
        return DriverDefinition.builder()
            .id("MOCK_DRIVER")
            .name("Mock Driver")
            .description("Mock driver for testing")
            .version("1.0.0")
            .tags(List.of("test", "mock"))
            .build();
    }

    @Override
    public void initialize(Datasource datasource) {

    }

    @Override
    public boolean connect() {
        return false;
    }

    @Override
    public void disconnect() {

    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public List<Reading> read(List<DeviceCommand> commands) throws Exception {
        return null;
    }

    @Override
    public void write(List<DeviceCommand> commands) throws Exception {

    }

    // Rest of the implementation stays the same
    
    // Use the datasource field in some method to avoid unused warning
    private String getConfigValue(String key, String defaultValue) {
        if (datasource == null || datasource.getConfiguration() == null) {
            return defaultValue;
        }
        
        return datasource.getConfiguration().stream()
            .filter(p -> key.equals(p.getName()))
            .findFirst()
            .map(Property::getValue)
            .orElse(defaultValue);
    }
} 