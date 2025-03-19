package com.sinelec.stage.domain.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.mvel2.MVEL;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * // Example 1: Simple comparison
 * Expression tempCheck = Expression.builder()
 *     .expression("temperature > threshold")
 *     .description("Check if temperature exceeds threshold")
 *     .build();
 *
 * Map<String, Object> vars = new HashMap<>();
 * vars.put("temperature", 25.5);
 * vars.put("threshold", 20.0);
 *
 * boolean result = tempCheck.evaluate(vars);  // Returns true
 *
 * // Example 2: More complex expression with multiple conditions
 * Expression complexCheck = Expression.builder()
 *     .expression("temperature > 20 && humidity < 80 || (systemMode == 'OVERRIDE' && manualFlag)")
 *     .build();
 *
 * Map<String, Object> context = new HashMap<>();
 * context.put("temperature", 18.0);
 * context.put("humidity", 75.0);
 * context.put("systemMode", "OVERRIDE");
 * context.put("manualFlag", true);
 *
 * boolean complexResult = complexCheck.evaluate(context);  // Returns true
 *
 * // Example 3: Static evaluation
 * boolean quickResult = Expression.evaluateStatic(
 *     "value.startsWith('ERR') || errorCode > 0",
 *     Map.of("value", "ERR_CONNECTION", "errorCode", 0)
 * );  // Returns true
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Expression {
    // The expression string to evaluate
    private String expression;
    
    // Optional description of what this expression does
    private String description;
    
    // Compiled expression for better performance when evaluated multiple times
    private transient Serializable compiledExpression;
    
    /**
     * Create a new expression with code and description
     * 
     * @param expression The expression code/formula to evaluate
     * @param description Human-readable description of the expression
     */
    public Expression(String expression, String description) {
        this.expression = expression;
        this.description = description;
    }
    
    /**
     * Evaluate the expression with the provided variables
     * 
     * @param variables Map of variable names to values available in the expression
     * @return boolean result of expression evaluation
     */
    public boolean evaluate(Map<String, Object> variables) {
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }
        
        try {
            // Compile expression if not already compiled
            if (compiledExpression == null) {
                compiledExpression = MVEL.compileExpression(expression);
            }
            
            // Evaluate the expression
            Object result = MVEL.executeExpression(compiledExpression, variables);
            
            // Convert result to boolean
            if (result instanceof Boolean) {
                return (Boolean) result;
            } else if (result == null) {
                return false;
            } else {
                // For non-boolean results, return true if not null/empty/zero
                return Boolean.parseBoolean(result.toString());
            }
        } catch (Exception e) {
            // Log the exception if needed
            return false;
        }
    }
    
    /**
     * Evaluate the expression with no variables
     * 
     * @return boolean result of expression evaluation
     */
    public boolean evaluate() {
        return evaluate(new HashMap<>());
    }
    
    /**
     * Helper method to create an expression and evaluate it immediately
     * 
     * @param expressionString The expression to evaluate
     * @param variables Variables to use during evaluation
     * @return boolean result of evaluation
     */
    public static boolean evaluateStatic(String expressionString, Map<String, Object> variables) {
        return Expression.builder()
                .expression(expressionString)
                .build()
                .evaluate(variables);
    }
}