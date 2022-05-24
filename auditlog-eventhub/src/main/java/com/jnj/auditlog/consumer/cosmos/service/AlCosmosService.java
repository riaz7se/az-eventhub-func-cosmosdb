package com.jnj.auditlog.consumer.cosmos.service;

import com.jnj.auditlog.common.model.AlEventData;
import com.jnj.auditlog.consumer.cosmos.AlDataContainer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface AlCosmosService {
    void deleteAllItems();

    void deleteContainer() throws Exception;

    Mono<AlEventData> saveAlEventData(Mono<AlEventData> auditLogContainer);

    List<AlEventData> findAllAlByAppName(String appName);

    AlDataContainer findLatestByAppName(String appName);

    List<AlEventData> getAlByAppNameAndPath(String appName, Map<String, Object> nestedJsonPathMap);

    Flux<AlEventData> getAlByAppNameAndPath_Stream(String appName, Map<String, Object> nestedJsonPathMap);

    List<AlEventData> getAllData();

    Flux<AlEventData> getAllDataStream();
}
