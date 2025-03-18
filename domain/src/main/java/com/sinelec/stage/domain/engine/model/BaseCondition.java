package com.sinelec.stage.domain.engine.model;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Map;

@Data
public abstract class BaseCondition implements ScriptableCondition {
    private String id;
    
    @NotBlank
    private String name;
    
    private String code;
    
    @Size(max = 500)
    private String description;
    
    private Expression expression;
    
    // Base builder class for condition builders
    public static abstract class BaseConditionBuilder<B extends BaseConditionBuilder<B, T>, T extends BaseCondition> {
        private String id;
        private String name;
        private String code;
        private String description;
        private Expression expression;
        
        public B id(String id) {
            this.id = id;
            return self();
        }
        
        public B name(String name) {
            this.name = name;
            return self();
        }
        
        public B code(String code) {
            this.code = code;
            return self();
        }
        
        public B description(String description) {
            this.description = description;
            return self();
        }
        
        public B expression(Expression expression) {
            this.expression = expression;
            return self();
        }
        
        @SuppressWarnings("unchecked")
        private B self() {
            return (B) this;
        }
        
        protected abstract T createObject();
        
        public T build() {
            T obj = createObject();
            obj.setId(id);
            obj.setName(name);
            obj.setCode(code);
            obj.setDescription(description);
            obj.setExpression(expression);
            return obj;
        }
    }
    
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