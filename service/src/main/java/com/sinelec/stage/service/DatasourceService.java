package com.sinelec.stage.service;

import com.sinelec.stage.domain.engine.model.Datasource;
import com.sinelec.stage.domain.engine.model.DriverDefinition;
import com.sinelec.stage.domain.engine.model.Property;
import com.sinelec.stage.domain.engine.model.PropertyDefinition;
import com.sinelec.stage.domain.engine.model.ValidateCondition;
import com.sinelec.stage.domain.validation.ValidationError;
import com.sinelec.stage.domain.validation.ValidationResult;
import com.sinelec.stage.repository.DatasourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DatasourceService {
    
    private final DatasourceRepository datasourceRepository;
    private final DriverDefinitionService driverDefinitionService;
    
    @Value("${app.validation.enabled:true}")
    private boolean validationEnabled;
    
    @Autowired
    public DatasourceService(
            DatasourceRepository datasourceRepository,
            DriverDefinitionService driverDefinitionService) {
        this.datasourceRepository = datasourceRepository;
        this.driverDefinitionService = driverDefinitionService;
    }
    
    public List<Datasource> getAllDatasources() {
        return datasourceRepository.findAll();
    }
    
    public Optional<Datasource> getDatasourceById(String id) {
        return datasourceRepository.findById(id);
    }
    
    public List<Datasource> getDatasourcesByDriverId(String driverId) {
        return datasourceRepository.findByDriverId(driverId);
    }
    
    public List<Datasource> getActiveDatasources() {
        return datasourceRepository.findByActive(true);
    }
    
    public List<Datasource> getConnectedDatasources() {
        return datasourceRepository.findByConnected(true);
    }
    
    /**
     * Creates a new datasource after validating its driver and configuration properties.
     * 
     * @param datasource The datasource to create
     * @return The created datasource
     * @throws IllegalArgumentException if validation fails
     */
    public Datasource createDatasource(Datasource datasource) {
        if (validationEnabled) {
            ValidationResult validationResult = validateDatasource(datasource);
            
            if (!validationResult.isValid()) {
                throw new IllegalArgumentException("Datasource validation failed: " + 
                    validationResult.getErrorMessages());
            }
        }
        
        // Set creation/updated timestamps
        Date now = new Date();
        datasource.setCreatedAt(now);
        datasource.setUpdatedAt(now);
        
        return datasourceRepository.save(datasource);
    }
    
    /**
     * Updates an existing datasource after validating its driver and configuration.
     * 
     * @param id Datasource ID to update
     * @param datasourceDetails New datasource details
     * @return Optional containing the updated datasource if found, empty otherwise
     * @throws IllegalArgumentException if validation fails
     */
    public Optional<Datasource> updateDatasource(String id, Datasource datasourceDetails) {
        ValidationResult validationResult = validateDatasource(datasourceDetails);
        
        if (!validationResult.isValid()) {
            throw new IllegalArgumentException("Datasource validation failed: " + 
                validationResult.getErrorMessages());
        }
        
        return datasourceRepository.findById(id)
            .map(datasource -> {
                datasource.setName(datasourceDetails.getName());
                datasource.setDescription(datasourceDetails.getDescription());
                datasource.setDriverId(datasourceDetails.getDriverId());
                datasource.setActive(datasourceDetails.isActive());
                datasource.setConfiguration(datasourceDetails.getConfiguration());
                datasource.setUpdatedAt(new Date());
                return datasourceRepository.save(datasource);
            });
    }
    
    /**
     * Validates that a datasource references a valid driver and has all required configuration properties.
     * 
     * @param datasource The datasource to validate
     * @return ValidationResult containing any validation errors
     */
    private ValidationResult validateDatasource(Datasource datasource) {
        ValidationResult result = ValidationResult.valid();
        
        // Skip validation in test environments or if essential fields are missing
        if (datasource.getDriverId() == null) {
            return result;
        }
        
        // 1. Check that driverId exists
        Optional<DriverDefinition> driverDefinition = 
            driverDefinitionService.getDriverDefinitionById(datasource.getDriverId());
        
        if (driverDefinition.isEmpty()) {
            result.addError(ValidationError.invalidReference(
                "driverId", 
                "Driver", 
                datasource.getDriverId()
            ));
            // If driver doesn't exist, we can't validate properties, so return early
            return result;
        }
        
        // 2. Validate configuration properties against driver definition
        DriverDefinition driver = driverDefinition.get();
        
        // Only proceed with property validation if configuration exists
        if (datasource.getConfiguration() != null && !datasource.getConfiguration().isEmpty() &&
            driver.getConfigurationProperties() != null && !driver.getConfigurationProperties().isEmpty()) {
            
            // Create maps for easier lookup
            Map<String, Property> configProps = datasource.getConfiguration().stream()
                .collect(Collectors.toMap(Property::getName, p -> p, (p1, p2) -> p1));
                
            Map<String, PropertyDefinition> driverPropDefs = driver.getConfigurationProperties().stream()
                .collect(Collectors.toMap(PropertyDefinition::getName, p -> p, (p1, p2) -> p1));
            
            // Check that all required properties are provided
            for (PropertyDefinition propDef : driver.getConfigurationProperties()) {
                if (propDef.isRequired() && !configProps.containsKey(propDef.getName())) {
                    result.addError(ValidationError.required(
                        propDef.getName(),
                        propDef.getName()
                    ));
                }
            }
            
            // Validate property values against their definitions
            for (Property prop : datasource.getConfiguration()) {
                PropertyDefinition propDef = driverPropDefs.get(prop.getName());
                
                // If property is defined in the driver, validate it
                if (propDef != null) {
                    // Validate property value based on DataType
                    if (propDef.getValueType() != null) {
                        try {
                            switch (propDef.getValueType()) {
                                case DOUBLE:
                                case FLOAT:
                                case INTEGER:
                                case LONG:
                                case BIGDECIMAL:
                                    if (!propDef.getValueType().isValidValue(prop.getValue())) {
                                        result.addError(ValidationError.invalidValue(
                                            prop.getName(),
                                            prop.getName(),
                                            prop.getValue(),
                                            propDef.getValueType().toString()
                                        ));
                                    }
                                    break;
                                    
                                case BOOLEAN:
                                    if (!propDef.getValueType().isValidValue(prop.getValue())) {
                                        result.addError(ValidationError.invalidValue(
                                            prop.getName(),
                                            prop.getName(),
                                            prop.getValue(),
                                            "boolean"
                                        ));
                                    }
                                    break;
                                    
                                case STRING:
                                    // Check if the value is in the allowed values map
                                    if (propDef.getAllowedValues() != null && 
                                        !propDef.getAllowedValues().isEmpty() && 
                                        !propDef.getAllowedValues().containsKey(prop.getValue())) {
                                        
                                        result.addError(ValidationError.customValidation(
                                            prop.getName(),
                                            prop.getName(),
                                            "Value '" + prop.getValue() + "' is not one of the allowed values: " + 
                                            String.join(", ", propDef.getAllowedValues().keySet())
                                        ));
                                    }
                                    break;
                                    
                                default:
                                    // No specific validation for other types
                                    break;
                            }
                        } catch (Exception e) {
                            result.addError(ValidationError.customValidation(
                                prop.getName(),
                                prop.getName(),
                                "Validation failed: " + e.getMessage()
                            ));
                        }
                    }
                    
                    // Apply any custom validate conditions
                    if (propDef.getValidateConditions() != null) {
                        for (ValidateCondition condition : propDef.getValidateConditions()) {
                            try {
                                Map<String, Object> variables = new HashMap<>();
                                variables.put("value", prop.getValue());
                                
                                if (condition.getExpression() != null && 
                                    !condition.getExpression().evaluate(variables)) {
                                    
                                    result.addError(ValidationError.customValidation(
                                        prop.getName(),
                                        prop.getName(),
                                        condition.getDescription()
                                    ));
                                }
                            } catch (Exception e) {
                                result.addError(ValidationError.customValidation(
                                    prop.getName(),
                                    prop.getName(),
                                    "Validation condition failed: " + e.getMessage()
                                ));
                            }
                        }
                    }
                }
            }
        }
        
        return result;
    }
    
    public boolean deleteDatasource(String id) {
        return datasourceRepository.findById(id)
            .map(datasource -> {
                datasourceRepository.delete(datasource);
                return true;
            })
            .orElse(false);
    }
} 