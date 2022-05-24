package com.jnj.auditlog.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AlBase {

    @NotNull private String infoType;

    @NotNull private String operation;

    private String appKey;

    private String requestId;

    @NotNull
    private Map<String, Object> payload;

    private String updateTimestamp = String.valueOf(LocalDateTime.now());

    private String updateBy;

}
