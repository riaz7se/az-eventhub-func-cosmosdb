package com.jnj.auditlog.consumer.cosmos.controller;

import com.azure.spring.cloud.autoconfigure.condition.ConditionalOnAnyProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.jnj.auditlog.common.model.AlEventData;
import com.jnj.auditlog.consumer.cosmos.AlDataContainer;
import com.jnj.auditlog.consumer.cosmos.service.AlCosmosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotBlank;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@Validated
@Tag(name = "CosmosDbController", description = "Cosmos DB API")
@ConditionalOnAnyProperty(prefix = "auditlog.endpoint", name = "expose", havingValue = "true")
public class CosmosDbController {

    private static final Logger logger = LoggerFactory.getLogger(CosmosDbController.class);

    private AlCosmosService alCosmosService;

    @Autowired
    public CosmosDbController(AlCosmosService alCosmosService) {
        this.alCosmosService = alCosmosService;
    }

    @DeleteMapping(value = "/delete-all")
    public void deleteAllLogs() {
        alCosmosService.deleteAllItems();
    }

    @Operation(summary = "Deletes Cosmos DB Container. On Startup a new Container will gets created")
    @DeleteMapping(value = "/container")
    public void deleteContainer() throws Exception {
        alCosmosService.deleteContainer();
    }

    @Operation(summary = "Fetch Audit logs by nested json path inside payload as Object. i.e; {\"owner\": \"o50\",\"sourceConnectionId\": 100,\"adGroups\": [\"ag22\"]}")
    @PostMapping(value = "/auditLogFetch/data/{appName}")
    public List<AlEventData> getAlByNestedPath(@PathVariable @NotBlank String appName, @RequestBody Map<String, Object> nestedJsonPathMap) {
        return alCosmosService.getAlByAppNameAndPath(appName, nestedJsonPathMap);
    }

    @Operation(summary = "Fetch Audit logs by nested json path inside payload As Stream. i.e; {\"owner\": \"o50\",\"sourceConnectionId\": 100,\"adGroups\": [\"ag22\"]}")
    @PostMapping(value = "/auditLogFetch/stream/{appName}")
    public Flux<AlEventData> getAlByNestedPath_Stream(@PathVariable @NotBlank String appName, @RequestBody Map<String, Object> nestedJsonPathMap) {
        return alCosmosService.getAlByAppNameAndPath_Stream(appName, nestedJsonPathMap);
    }

    @Operation(summary = "Get All data from Container and return as Object")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All Container Audit Logs", content = {
                    @Content(mediaType = "application/x-ndjson", schema = @Schema(implementation = AlEventData.class))})})
    @GetMapping(value = "/all")
    public List<AlEventData> getAllData() {
        return alCosmosService.getAllData();
    }

    @Operation(summary = "Get All data from Container and return as Stream")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All Container Audit Logs", content = {
                    @Content(mediaType = "application/x-ndjson", schema = @Schema(implementation = AlEventData.class))})})
    @GetMapping(value = "/all/stream", produces = MediaType.APPLICATION_NDJSON_VALUE )
    public Flux<AlEventData> getAllDataStream() {
        return  alCosmosService.getAllDataStream();
    }

    @Operation(summary = "Post AuditLogs as Save to Cosmos DB as a Map Input data. i,e; {appName: App1, infoType: InfoA, auditObj: {any obj with respect to InfoType} }")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved AuditLogDataContainer", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = AlDataContainer.class))}),
            @ApiResponse(responseCode = "400", description = "AuditLogEventData invalid", content = @Content),
            @ApiResponse(responseCode = "404", description = "AuditLogDataContainer not saved", content = @Content) })
    @PostMapping(value = "/auditLogData/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<AlEventData> saveNewAuditLog(
            @RequestBody Mono<AlEventData> alData, ServerRequest serverRequest)  {
        String userName = serverRequest.principal()
                .map(Principal::getName).block();

        alData.subscribe(alEventData -> alEventData.setUpdateBy(userName));
        return alCosmosService.saveAlEventData(alData);
    }
}
