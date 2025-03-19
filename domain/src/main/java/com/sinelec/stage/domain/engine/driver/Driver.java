package com.sinelec.stage.domain.engine.driver;

import com.sinelec.stage.domain.engine.model.Datasource;
import com.sinelec.stage.domain.engine.model.Reading;
import com.sinelec.stage.domain.engine.model.DeviceCommand;

import java.util.List;

public interface Driver {
    /**
     * Get the unique driver ID
     */
    String getId();
    
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
    List<Reading> read(List<DeviceCommand> commands) throws Exception;
    
    /**
     * Write values to the specified signals
     */
    void write(List<DeviceCommand> commands) throws Exception;
} 