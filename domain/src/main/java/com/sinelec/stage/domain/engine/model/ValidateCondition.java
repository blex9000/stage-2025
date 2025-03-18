package com.sinelec.stage.domain.engine.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateCondition {
    @NonNull
    private String id;
    @NonNull
    private String code;
    @NonNull
    private String description;
    @NonNull
    private Expression expression;
}