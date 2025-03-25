package com.sinelec.stage.domain.engine.driver;

import com.sinelec.stage.domain.engine.model.Datasource;
import com.sinelec.stage.domain.engine.model.DriverDefinition;
import com.sinelec.stage.domain.engine.model.Reading;
import com.sinelec.stage.domain.engine.model.DeviceCommand;
import com.sinelec.stage.domain.engine.model.SignalDefinition;
import com.sinelec.stage.domain.engine.model.SignalConfiguration;

import java.util.List;

/**
 * Interface for device communication drivers
 */
public interface Driver {
    /**
     * Get the unique driver ID
     */
    String getId();
    
    /**
     * Get the driver definition for registration
     */
    DriverDefinition getDefinition();
    
    /**
     * Initialize the driver with the datasource configuration
     */
    void initialize(Datasource datasource);
    
    /**
     * Connect to the datasource
     */
    boolean connect();
    
    /**
     * Disconnect from the datasource
     */
    void disconnect();
    
    /**
     * Check if the driver is connected
     */
    boolean isConnected();
    
    /**
     * Read values for the specified signals
     */
    List<Reading> execute(List<DeviceCommand> commands) throws Exception;
    
    /**
     * Write values to the specified signals
     */
    void write(List<DeviceCommand> commands) throws Exception;
    
    /**
     * Find a signal definition in the command by signal ID
     */
    default SignalDefinition findSignalDefinition(DeviceCommand command, String signalId) {
        if (command == null || command.getSignalDefinitions() == null) {
            return null;
        }
        return command.getSignalDefinitions().stream()
                .filter(def -> signalId.equals(def.getId()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Find a signal configuration in the command by signal ID
     */
    default SignalConfiguration findSignalConfiguration(DeviceCommand command, String signalId) {
        if (command == null || command.getSignalConfigurations() == null) {
            return null;
        }
        return command.getSignalConfigurations().stream()
                .filter(conf -> signalId.equals(conf.getSignalId()))
                .findFirst()
                .orElse(null);
    }

} 