package com.sinelec.stage.engine.core;

import com.sinelec.stage.domain.engine.driver.Driver;
import com.sinelec.stage.domain.engine.model.DriverDefinition;
import com.sinelec.stage.service.DriverDefinitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class DriverRegistry {
    private static final Logger logger = LoggerFactory.getLogger(DriverRegistry.class);
    
    private final ApplicationContext applicationContext;
    private final DriverDefinitionService driverDefinitionService;
    private final Map<String, Class<? extends Driver>> driverClasses = new HashMap<>();
    
    @Autowired
    public DriverRegistry(
            ApplicationContext applicationContext,
            DriverDefinitionService driverDefinitionService) {
        this.applicationContext = applicationContext;
        this.driverDefinitionService = driverDefinitionService;
    }
    
    @PostConstruct
    public void initialize() {
        // Discover all Driver implementations
        Map<String, Driver> driverBeans = applicationContext.getBeansOfType(Driver.class);
        
        logger.info("Discovered {} driver implementations", driverBeans.size());
        
        // Register each driver and update its definition in the database
        for (Driver driver : driverBeans.values()) {
            registerDriver(driver);
        }
    }
    
    private void registerDriver(Driver driver) {
        try {
            String driverId = driver.getId();
            DriverDefinition definition = driver.getDefinition();
            
            // Store the driver class for later instantiation
            driverClasses.put(driverId, driver.getClass());
            
            // Update or create the driver definition in the database
            Optional<DriverDefinition> existingDef = 
                driverDefinitionService.getDriverDefinitionById(driverId);
                
            if (existingDef.isPresent()) {
                // Update existing definition if needed
                DriverDefinition updatedDef = existingDef.get();
                updatedDef.setName(definition.getName());
                updatedDef.setDescription(definition.getDescription());
                updatedDef.setVersion(definition.getVersion());
                updatedDef.setTags(definition.getTags());
                updatedDef.setConnectionProperties(definition.getConnectionProperties());
                driverDefinitionService.updateDriverDefinition(driverId, updatedDef);
                logger.info("Updated driver definition for {}", driverId);
            } else {
                // Create new definition
                driverDefinitionService.createDriverDefinition(definition);
                logger.info("Created new driver definition for {}", driverId);
            }
        } catch (Exception e) {
            logger.error("Failed to register driver: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Create a new instance of a driver by its ID
     */
    public Driver createDriver(String driverId) {
        Class<? extends Driver> driverClass = driverClasses.get(driverId);
        if (driverClass == null) {
            throw new IllegalArgumentException("Driver not found: " + driverId);
        }
        
        // Use Spring to create a new prototype instance
        return applicationContext.getBean(driverClass);
    }
    
    /**
     * Get all available driver IDs
     */
    public List<String> getAvailableDriverIds() {
        return List.copyOf(driverClasses.keySet());
    }
    
    /**
     * Check if a driver is available
     */
    public boolean isDriverAvailable(String driverId) {
        return driverClasses.containsKey(driverId);
    }
} 