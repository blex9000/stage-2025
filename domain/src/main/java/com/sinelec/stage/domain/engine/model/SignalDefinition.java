package com.sinelec.stage.domain.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignalDefinition {
    private String id;
    private String name;
    private String description;
    private SignalDataType type;
    // Alarm configuration
    private boolean alarmsEnabled;
    private List<AlarmCondition> alarmConditions;
    private List<ValidateCondition> validateConditions;
    // Unit of measurement
    private String unit;
}