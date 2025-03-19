package com.sinelec.stage.domain.engine.model;

import lombok.*;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;

@Getter
@Setter
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

