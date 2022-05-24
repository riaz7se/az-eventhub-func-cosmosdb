package com.jnj.auditlog.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("auditlog")
public class AlProperties {
    private String appName;

    private boolean exceptionLog;
}
