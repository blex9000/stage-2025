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
@Document(collection = "datasources")
public class Datasource {
    @Id
    private String id;
    private String name;
    private String description;
    private String driverId;
    private boolean active;

    @Builder.Default
    private List<Property> configuration = new ArrayList<>();
    
    // Connection info
    private Date lastConnection;
    private boolean connected;
    private String connectionStatus;
    
    // Audit fields
    private Date createdAt;
    private String createdBy;
    private Date updatedAt;
    private String updatedBy;
}