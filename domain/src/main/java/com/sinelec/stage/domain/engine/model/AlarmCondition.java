package com.sinelec.stage.domain.engine.model;

import lombok.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AlarmCondition extends BaseCondition {
    private AlarmSeverity severity;
}