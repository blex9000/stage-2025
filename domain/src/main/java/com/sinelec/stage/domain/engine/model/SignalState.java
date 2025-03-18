package com.sinelec.stage.domain.engine.model;

import lombok.*;

import java.util.Map;
import java.util.HashMap;

@Data
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class SignalState {
    private String signalId;
    private Reading lastReading;
}