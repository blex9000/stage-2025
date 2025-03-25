package com.sinelec.stage.test.integration;

import com.sinelec.stage.domain.engine.model.DriverDefinition;
import com.sinelec.stage.domain.engine.model.PropertyDefinition;
import com.sinelec.stage.repository.DriverDefinitionRepository;
import com.sinelec.stage.service.DeviceDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.sinelec.stage.domain.engine.model.*;

/**
 * Utility class to create test data for integration tests
 */
@Component
public class TestDataUtil {
    
    private final DriverDefinitionRepository driverDefinitionRepository;
    private final DeviceDefinitionService deviceDefinitionService;
    
    @Autowired
    public TestDataUtil(DriverDefinitionRepository driverDefinitionRepository, 
                         DeviceDefinitionService deviceDefinitionService) {
        this.driverDefinitionRepository = driverDefinitionRepository;
        this.deviceDefinitionService = deviceDefinitionService;
    }
    
    /**
     * Initialize test driver data
     */
    public DriverDefinition createModbusDriverDefinition() {
        // Check if already exists
        return driverDefinitionRepository.findById("MODBUS_DRIVER")
            .orElseGet(() -> {
                // Create Modbus TCP driver definition
                DriverDefinition driver = DriverDefinition.builder()
                    .id("MODBUS_DRIVER")
                    .name("Modbus TCP Driver")
                    .description("Driver for Modbus TCP protocol")
                    .version("1.0.0")
                    .tags(List.of("MODBUS_TCP"))
                    .build();
                
                // Configuration properties
                List<PropertyDefinition> properties = new ArrayList<>();
                properties.add(PropertyDefinition.builder()
                    .name("HOST")
                    .description("Host or IP address of the device")
                    .required(true)
                    .valueType(DataType.STRING)
                    .build());
                
                properties.add(PropertyDefinition.builder()
                    .name("PORT")
                    .description("TCP port")
                    .required(true)
                    .valueType(DataType.INTEGER)
                    .defaultValue("502")
                    .build());
                
                properties.add(PropertyDefinition.builder()
                    .name("KEEPALIVE")
                    .description("Keep connection alive")
                    .required(false)
                    .valueType(DataType.BOOLEAN)
                    .defaultValue("true")
                    .build());
                
                properties.add(PropertyDefinition.builder()
                    .name("CONNECTION_TIMEOUT")
                    .description("Connection timeout in milliseconds")
                    .required(false)
                    .valueType(DataType.INTEGER)
                    .defaultValue("1000")
                    .build());
                
                driver.setConnectionProperties(properties);
                driver.setCreatedAt(new Date());
                driver.setUpdatedAt(new Date());
                
                return driverDefinitionRepository.save(driver);
            });
    }
    
    /**
     * Create a test device definition with predefined signals
     */
    public DeviceDefinition createTestDeviceDefinition() {
        // Create device definition for testing
        DeviceDefinition deviceDefinition = new DeviceDefinition();
        deviceDefinition.setName("Test Device Definition");
        deviceDefinition.setDescription("Definition for automated testing");
        
        // Create signal definitions
        List<SignalDefinition> signals = new ArrayList<>();
        
        // Temperature signal
        SignalDefinition tempSignal = SignalDefinition.builder()
            .id("temp-signal-id")
            .name("TEMPERATURE")
            .description("Temperature measurement")
            .type(DataType.FLOAT)
            .unit("°C")
            .alarmsEnabled(true)
            .required(true)
            .build();
            
        signals.add(tempSignal);
        
        // Status signal
        SignalDefinition statusSignal = SignalDefinition.builder()
            .id("status-signal-id")
            .name("STATUS")
            .description("Device status")
            .type(DataType.BOOLEAN)
            .required(true)
            .build();
            
        signals.add(statusSignal);
        
        deviceDefinition.setSignals(signals);
        
        // Save or retrieve device definition - using deviceDefinitionService instead of deviceService
        return deviceDefinitionService.createDeviceDefinition(deviceDefinition);
    }
} 