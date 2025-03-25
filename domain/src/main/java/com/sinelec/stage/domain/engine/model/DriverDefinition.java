package com.sinelec.stage.domain.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
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
@Document(collection = "driverDefinitions")
public class DriverDefinition {
    @Id
    private String id;
    private String name;
    private String description;
    private String version;

    @Builder.Default
    private List<PropertyDefinition> connectionProperties = new ArrayList<>();
    
    @Builder.Default
    private List<PropertyDefinition> signalProperties = new ArrayList<>();

    @Builder.Default
    private List<String> tags = new ArrayList<>();
    
    private Date createdAt;
    private Date updatedAt;
}

