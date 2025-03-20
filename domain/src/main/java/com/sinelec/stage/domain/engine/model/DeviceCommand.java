package com.sinelec.stage.domain.engine.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "deviceCommands")
public class DeviceCommand {
    @Id
    @NonNull
    private String id;
    @NonNull
    private String datasourceId;
    @NonNull
    private String deviceId;

    @Builder.Default
    private CommandType commandType = CommandType.READ;

    @Transient
    private List<SignalConfiguration> signalConfigurations;
    @Transient
    private List<SignalDefinition> signalDefinitions;

    @Builder.Default
    private List<Write> write = new ArrayList<>();
    @Builder.Default
    private List<Read> read = new ArrayList<>();
    
    private Date createdAt;
    private String createdBy;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private CommandStatus status;
    private Date sentAt;
    private Date completedAt;
    private String resultMessage;
    private Integer retryCount;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public enum CommandStatus {
        PENDING, SENT, ACKNOWLEDGED, COMPLETED, FAILED, CANCELLED
    }

    public enum CommandType {
        WRITE, READ
    }

    @Getter
    @Setter 
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Write {
        private String signalId;
        private String value;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Read {
        private String signalId;
    }
} 