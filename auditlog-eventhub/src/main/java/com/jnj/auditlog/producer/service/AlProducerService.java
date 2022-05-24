package com.jnj.auditlog.producer.service;

import com.jnj.auditlog.common.annotation.AuditLog;
import com.jnj.auditlog.common.model.AlEventData;
import com.jnj.auditlog.consumer.cosmos.AlDataContainer;
import reactor.core.publisher.Mono;

public interface AlProducerService {

    @AuditLog
    default void produceAuditLog(AlEventData alDataFromInput){
    }

    Mono<AlDataContainer> saveAuditLog(AlDataContainer alDataFromInput);
}
