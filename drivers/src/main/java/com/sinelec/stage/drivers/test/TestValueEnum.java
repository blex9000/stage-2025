package com.sinelec.stage.drivers.test;

/**
 * Enum of predefined values that can be used for static signal configuration
 */
public enum TestValueEnum {
    LOW("LOW", 0),
    MEDIUM("MEDIUM", 50),
    HIGH("HIGH", 100),
    ON("ON", 1),
    OFF("OFF", 0),
    ERROR("ERROR", -1);
    
    private final String name;
    private final int numericValue;
    
    TestValueEnum(String name, int numericValue) {
        this.name = name;
        this.numericValue = numericValue;
    }
    
    public String getName() {
        return name;
    }
    
    public int getNumericValue() {
        return numericValue;
    }
    
    @Override
    public String toString() {
        return name;
    }
} 