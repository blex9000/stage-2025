package com.sinelec.stage.engine.integration;

import com.sinelec.stage.domain.engine.model.DriverDefinition;
import com.sinelec.stage.domain.engine.model.PropertyDefinition;
import com.sinelec.stage.repository.DriverDefinitionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sinelec.stage.domain.engine.model.*;

/**
 * Utility class to create test data for integration tests
 */
@Component
public class TestDataUtil {
    
    private final DriverDefinitionRepository driverDefinitionRepository;
    
    @Autowired
    public TestDataUtil(DriverDefinitionRepository driverDefinitionRepository) {
        this.driverDefinitionRepository = driverDefinitionRepository;
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
                
                driver.setConfigurationProperties(properties);
                driver.setCreatedAt(new Date());
                driver.setUpdatedAt(new Date());
                
                return driverDefinitionRepository.save(driver);
            });
    }
} 