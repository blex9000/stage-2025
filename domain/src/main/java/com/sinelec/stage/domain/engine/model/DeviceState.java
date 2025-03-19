package com.sinelec.stage.domain.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Document(collection = "deviceStates")
public class DeviceState {
    @Id
    private String id;
    private String deviceId;
    private Date created;
    private Date updated;
    
    @Builder.Default
    private List<SignalState> signalStates = new ArrayList<>();
    
    private boolean connected;
    private String connectionStatus;
    private HealthStatus healthStatus;
}

