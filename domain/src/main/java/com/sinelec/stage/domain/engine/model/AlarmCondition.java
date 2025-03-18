package com.sinelec.stage.domain.engine.model;

import lombok.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlarmCondition {
    @NonNull
    private String id;
    @NotBlank
    private String name;
    @NonNull
    private String code;
    @Size(max = 500)
    private String description;
    @NonNull
    private Expression expression;

    private AlarmSeverity severity;
}