package com.sinelec.stage.domain.engine.model;

import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
public class ValidateCondition extends BaseCondition {
    // Any validation-specific fields
    
    // Use a builder that properly handles superclass fields
    public static class ValidateConditionBuilder extends BaseConditionBuilder<ValidateConditionBuilder, ValidateCondition> {
        @Override
        protected ValidateCondition createObject() {
            return new ValidateCondition();
        }
    }
    
    public static ValidateConditionBuilder builder() {
        return new ValidateConditionBuilder();
    }
}