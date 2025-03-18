package com.sinelec.stage.domain.engine.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SignalDataType {
    NUMERIC("Numeric", "Number value with optional decimal places", Double.class),
    INTEGER("Integer", "Whole number value", Integer.class),
    BOOLEAN("Boolean", "True/false value", Boolean.class),
    STRING("String", "Text value", String.class);
    
    private final String label;
    private final String description;
    private final Class<?> javaType;
    
    /**
     * Validate if a value matches this data type
     */
    public boolean isValidValue(Object value) {
        if (value == null) {
            return true; // Null values are allowed for all types
        }
        
        switch (this) {
            case NUMERIC:
                return value instanceof Number ||
                       (value instanceof String && isNumeric((String)value));
            case INTEGER:
                return value instanceof Integer ||
                       (value instanceof String && isInteger((String)value));
            case BOOLEAN:
                return value instanceof Boolean ||
                       (value instanceof String && isBoolean((String)value));
            default:
                return true; // All other types accept any non-null value
        }
    }
    
    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private boolean isBoolean(String str) {
        return "true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str);
    }
    
    private boolean isValidDate(String str) {
        try {
            // Simple check - could use DateTimeFormatter for better validation
            return java.time.Instant.parse(str) != null;
        } catch (Exception e) {
            return false;
        }
    }
}