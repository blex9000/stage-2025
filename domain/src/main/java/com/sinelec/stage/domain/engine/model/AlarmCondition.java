package com.sinelec.stage.domain.engine.model;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
@EqualsAndHashCode(callSuper = true)
public class AlarmCondition extends BaseCondition {
    private AlarmSeverity severity;
    
    // Use a builder that properly handles superclass fields
    public static class AlarmConditionBuilder extends BaseConditionBuilder<AlarmConditionBuilder, AlarmCondition> {
        private AlarmSeverity severity;
        
        public AlarmConditionBuilder severity(AlarmSeverity severity) {
            this.severity = severity;
            return this;
        }
        
        @Override
        protected AlarmCondition createObject() {
            AlarmCondition condition = new AlarmCondition();
            condition.setSeverity(severity);
            return condition;
        }
    }
    
    public static AlarmConditionBuilder builder() {
        return new AlarmConditionBuilder();
    }
}