package com.sinelec.stage.domain.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contains the result of a validation operation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {
    @Builder.Default
    private List<ValidationError> errors = new ArrayList<>();
    
    /**
     * Check if validation succeeded (no errors)
     */
    public boolean isValid() {
        return errors == null || errors.isEmpty();
    }
    
    /**
     * Add a new validation error
     */
    public ValidationResult addError(ValidationError error) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(error);
        return this;
    }
    
    /**
     * Combine multiple validation results
     */
    public ValidationResult merge(ValidationResult other) {
        if (other != null && other.getErrors() != null) {
            if (this.errors == null) {
                this.errors = new ArrayList<>();
            }
            this.errors.addAll(other.getErrors());
        }
        return this;
    }
    
    /**
     * Get a formatted string with all error messages
     */
    public String getErrorMessages() {
        if (isValid()) {
            return "";
        }
        return errors.stream()
            .map(ValidationError::getErrorMessage)
            .collect(Collectors.joining("; "));
    }
    
    /**
     * Create a successful validation result
     */
    public static ValidationResult valid() {
        return new ValidationResult();
    }
    
    /**
     * Create a validation result with a single error
     */
    public static ValidationResult invalid(ValidationError error) {
        ValidationResult result = new ValidationResult();
        result.addError(error);
        return result;
    }
} 