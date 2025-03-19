package com.sinelec.stage.domain.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Property {
    private String id;  
    private String name;
    private String value;
    
    /**
     * Constructor with just name and value for convenience
     */
    public Property(String name, String value) {
        this.name = name;
        this.value = value;
    }
} 