package com.jnj.auditlog.consumer.cosmos.repo;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.azure.spring.data.cosmos.repository.Query;
import com.jnj.auditlog.consumer.cosmos.AlDataContainer;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AlCosmosRepository extends CosmosRepository<AlDataContainer, String> {

    Iterable<AlDataContainer> findByAppName(String appName);

    long countByAppName(String appName);

//    AuditLogDataContainer findOne(String infoType, String appName);

//    AuditLogDataContainer findByAppName(String lastName);

    @Query("select * from c where c.appName = @appName and c.infoType = @infoType")
    List<AlDataContainer> getAuditLogsByFirstNameAndLastName(@Param("appName") String appName, @Param("infoType") String infoType);

    @Query("select * from c where c.appName = @appName and @path = @value")
    List<AlDataContainer> getAlByAppNameAndPath(@Param("appName") String appName, @Param("path") String path, @Param("value") String value);


    @Query("select * from c offset @offset limit @limit")
    List<AlDataContainer> getAuditLogsWithOffsetLimit(@Param("offset") int offset, @Param("limit") int limit);

    @Query("select value count(1) from c where c.appName = @appName")
    long getNumberOfAuditLogsWithAppName(@Param("appName") String appName);

    Slice<AlDataContainer> findByAppName(String appName, Pageable pageRequest);
}
