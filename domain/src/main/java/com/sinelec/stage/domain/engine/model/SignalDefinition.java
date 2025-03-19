package com.sinelec.stage.domain.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.ArrayList;

/**
 * Defines a signal that can be monitored or controlled on a device
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignalDefinition {
    private String id;
    private String name;
    private String description;
    private List<String> tags;
    private DataType type;
    private String unit;
    private boolean alarmsEnabled;
    private boolean required = false;  // Default to not required
    // Alarm configuration
    @Builder.Default
    private List<AlarmCondition> alarmConditions = new ArrayList<>();
    @Builder.Default
    private List<ValidateCondition> validateConditions = new ArrayList<>();
}