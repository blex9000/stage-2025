package com.sinelec.stage.domain.engine.model;

import java.util.Map;

/**
 * Common interface for conditions that can be evaluated
 */
public interface ScriptableCondition {
    /**
     * Get the condition's unique identifier
     */
    String getId();
    
    /**
     * Get the condition's code
     */
    String getCode();
    
    /**
     * Get the condition's description
     */
    String getDescription();
    
    /**
     * Get the expression used to evaluate the condition
     */
    Expression getExpression();
    
    /**
     * Evaluate the condition using the provided context
     * 
     * @param variables Variables to use during evaluation
     * @return true if the condition is met, false otherwise
     */
    default boolean evaluate(Map<String, Object> variables) {
        return getExpression() != null && getExpression().evaluate(variables);
    }
    
    /**
     * Evaluate the condition against a reading
     * 
     * @param reading Reading to evaluate
     * @return true if the condition is met, false otherwise
     */
    default boolean evaluate(Reading reading) {
        if (reading == null || getExpression() == null) {
            return false;
        }
        
        Map<String, Object> variables = Map.of(
            "value", reading.getValue(),
            "numericValue", reading.getNumericValue(),
            "reading", reading
        );
        
        return getExpression().evaluate(variables);
    }
} 