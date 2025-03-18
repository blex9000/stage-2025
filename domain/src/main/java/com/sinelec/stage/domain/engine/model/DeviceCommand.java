package com.sinelec.stage.domain.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

enum CommandType {
    WRITE, READ
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "deviceCommands")
public class DeviceCommand {
    @Id
    private String id;
    private String datasourceId;
    private String deviceId;
    private String signalId;
    private CommandType commandType;

    @Transient
    private SignalConfiguration signalConfiguration;
    @Transient
    private SignalDefinition signalDefinition;

    @Builder.Default
    private Map<String, String> parameters = new HashMap<>();
    
    private Date createdAt;
    private String createdBy;
    
    private CommandStatus status;
    private Date sentAt;
    private Date completedAt;
    private String resultMessage;
    private Integer retryCount;
    
    public enum CommandStatus {
        PENDING, SENT, ACKNOWLEDGED, COMPLETED, FAILED, CANCELLED
    }
} 