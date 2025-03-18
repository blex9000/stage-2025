package com.sinelec.stage.domain.engine.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AlarmSeverity {
    INFO(0, "Information"),
    WARNING(1, "Warning"),
    MINOR(2, "Minor"),
    MAJOR(3, "Major"),
    CRITICAL(4, "Critical");
    
    private final int level;
    private final String description;
}