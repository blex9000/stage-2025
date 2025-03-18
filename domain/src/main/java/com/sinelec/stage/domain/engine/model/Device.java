package com.sinelec.stage.domain.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Document(collection = "devices")
public class Device {
    @Id
    private String id;
    private String name;
    private String description;
    private String location;
    private boolean active;
    private String deviceDefinitionId;
    private String datasourceId;
    
    @Builder.Default
    private List<SignalConfiguration> signalConfigurations = new ArrayList<>();
    
    // Audit fields
    private Date createdAt;
    private String createdBy;
    private Date updatedAt;
    private String updatedBy;
}