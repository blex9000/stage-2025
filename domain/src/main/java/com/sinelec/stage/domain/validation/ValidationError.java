package com.sinelec.stage.domain.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a validation error with detailed information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationError {
    private String propertyId;       // ID of the property/field with the error
    private String propertyName;     // Name of the property/field 
    private String errorMessage;     // Human-readable error message
    private ErrorSeverity severity;  // Severity level of the error
    private String errorCode;        // Optional error code for programmatic handling
    
    // Factory methods for common error types
    public static ValidationError required(String propertyId, String propertyName) {
        return ValidationError.builder()
            .propertyId(propertyId)
            .propertyName(propertyName)
            .errorMessage(propertyName + " is required")
            .severity(ErrorSeverity.ERROR)
            .errorCode("REQUIRED")
            .build();
    }
    
    public static ValidationError invalidValue(String propertyId, String propertyName, String value, String expectedType) {
        return ValidationError.builder()
            .propertyId(propertyId)
            .propertyName(propertyName)
            .errorMessage(propertyName + " value '" + value + "' is not a valid " + expectedType)
            .severity(ErrorSeverity.ERROR)
            .errorCode("INVALID_VALUE")
            .build();
    }
    
    public static ValidationError invalidReference(String propertyId, String propertyName, String value) {
        return ValidationError.builder()
            .propertyId(propertyId)
            .propertyName(propertyName)
            .errorMessage(propertyName + " with ID '" + value + "' not found")
            .severity(ErrorSeverity.ERROR)
            .errorCode("INVALID_REFERENCE")
            .build();
    }
    
    public static ValidationError customValidation(String propertyId, String propertyName, String message) {
        return ValidationError.builder()
            .propertyId(propertyId)
            .propertyName(propertyName)
            .errorMessage(message)
            .severity(ErrorSeverity.ERROR)
            .errorCode("CUSTOM_VALIDATION")
            .build();
    }
    
    public enum ErrorSeverity {
        WARNING, ERROR, CRITICAL
    }
} 