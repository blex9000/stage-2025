package com.sinelec.stage.domain.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyDefinition {
    private String name;
    private String description;
    private boolean required;
    private String defaultValue;
    private CollectionType collectionType;
    private DataType valueType;
    @Builder.Default
    private Map<String, String> allowedValues = new HashMap<>();
    @Builder.Default
    private List<ValidateCondition> validateConditions = new ArrayList<>();
}
