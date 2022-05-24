// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.jnj.auditlog.consumer.cosmos.repo;

import com.azure.spring.data.cosmos.repository.Query;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import com.jnj.auditlog.consumer.cosmos.AlDataContainer;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface AlReactiveRepository extends ReactiveCosmosRepository<AlDataContainer, String> {

    Flux<AlDataContainer> findByAppName(String appName);

    @Query(value = "select * from c where c.appName in (@appName1,@appName2)")
    Flux<AlDataContainer> findByAppNamesQuery(String appName1, String appName2);
}

