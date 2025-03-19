package com.sinelec.stage.domain.engine.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@RequiredArgsConstructor
public enum DataType {
    DOUBLE("Numeric", "Number value with optional decimal places", Double.class),
    FLOAT("Float", "Number value with optional decimal places", Float.class),
    INTEGER("Integer", "Whole number value", Integer.class),
    BOOLEAN("Boolean", "True/false value", Boolean.class),
    STRING("String", "Text value", String.class),
    LONG("Long", "Large integer value", Long.class),
    BIGDECIMAL("BigDecimal", "High precision decimal value", BigDecimal.class),
    DATE("Date", "Calendar date value", Date.class),
    DATETIME("DateTime", "Date and time value", LocalDateTime.class),
    TIMESTAMP("Timestamp", "Instant timestamp value", Instant.class),
    BINARY("Binary", "Binary data", byte[].class);
    
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
            case DOUBLE:
                return value instanceof Number ||
                       (value instanceof String && isNumeric((String)value));
            case FLOAT:
                return value instanceof Number ||
                       (value instanceof String && isNumeric((String)value));
            case INTEGER:
                return value instanceof Integer ||
                       (value instanceof String && isInteger((String)value));
            case BOOLEAN:
                return value instanceof Boolean ||
                       (value instanceof String && isBoolean((String)value));
            case LONG:
                return value instanceof Long ||
                       (value instanceof String && isLong((String)value));
            case BIGDECIMAL:
                return value instanceof BigDecimal ||
                       (value instanceof String && isBigDecimal((String)value));
            case DATE:
                return value instanceof Date ||
                       (value instanceof String && isDate((String)value));
            case DATETIME:
                return value instanceof LocalDateTime ||
                       (value instanceof String && isDateTime((String)value));
            case TIMESTAMP:
                return value instanceof Instant ||
                       (value instanceof String && isTimestamp((String)value));
            case BINARY:
                return value instanceof byte[];
            default:
                return true; // String type accepts any non-null value as string
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
    
    private boolean isLong(String str) {
        try {
            Long.parseLong(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private boolean isBigDecimal(String str) {
        try {
            new BigDecimal(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private boolean isBoolean(String str) {
        return "true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str);
    }
    
    private boolean isDate(String str) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            sdf.parse(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean isDateTime(String str) {
        try {
            LocalDateTime.parse(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean isTimestamp(String str) {
        try {
            Instant.parse(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}