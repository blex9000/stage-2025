package com.sinelec.stage.domain.engine.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum HealthStatus {
    UNKNOWN("Unknown", "Connection state unknown"),
    HEALTHY("Healthy", "Device is functioning normally"),
    DEGRADED("Degraded", "Device is functioning with some issues"),
    CRITICAL("Critical", "Device has critical issues"),
    MAINTENANCE("Maintenance", "Device is in maintenance mode");
    
    private final String label;
    private final String description;
} 