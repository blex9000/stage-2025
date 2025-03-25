package com.sinelec.stage.drivers.test;

/**
 * Interface for signal property generators in the test driver
 */
public interface TestSignalProperty {
    /**
     * Generate a value for the signal
     * @return Generated value object
     */
    Object generateValue();
} 