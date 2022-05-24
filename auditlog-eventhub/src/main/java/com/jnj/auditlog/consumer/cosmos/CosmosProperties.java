// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.jnj.auditlog.consumer.cosmos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cosmos")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CosmosProperties {

    private String uri;

    private String key;

    private String container;

    private String database;

    private String secondaryKey;

    private boolean queryMetricsEnabled;
}
