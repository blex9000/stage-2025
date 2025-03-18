package com.sinelec.stage.domain.engine.model;

import lombok.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class BaseCondition implements ScriptableCondition {
    @NonNull
    private String id;
    
    @NotBlank
    private String name;
    
    @NonNull
    private String code;
    
    @Size(max = 500)
    private String description;
    
    @NonNull
    private Expression expression;
    
    @Override
    public boolean evaluate(Map<String, Object> variables) {
        return expression != null && expression.evaluate(variables);
    }
    
    @Override
    public boolean evaluate(Reading reading) {
        if (reading == null || expression == null) {
            return false;
        }
        
        Map<String, Object> variables = Map.of(
            "value", reading.getValue(),
            "numericValue", reading.getNumericValue(),
            "reading", reading
        );
        
        return expression.evaluate(variables);
    }
} 