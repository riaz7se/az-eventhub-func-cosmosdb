package com.jnj.auditlog.consumer.cosmos;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.GeneratedValue;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jnj.auditlog.common.model.AlBase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Container(containerName = "auditlog", partitionKeyPath = "/appName")
@JsonIgnoreProperties(ignoreUnknown = true)
//@Container(containerName = "auditlog", autoScale = true, ru = "4000")
//@Container(containerName = "auditlog", partitionKeyPath = "/appName", nestedPartionKey = "/payload/ops")
public class AlDataContainer extends AlBase {

    @Id
    @GeneratedValue
    private String auditId;

    @PartitionKey
    private String appName;

}
