package com.jnj.auditlog.producer.controller;

import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jnj.auditlog.common.model.AlEventData;
import com.jnj.auditlog.consumer.cosmos.AlDataContainer;
import com.jnj.auditlog.producer.service.AlProducerService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "AlProducerController", description = "Audit Log Producer API")
public class AlProducerController {
    private static final Logger logger = LoggerFactory.getLogger(AlProducerController.class);

    private AlProducerService alProducerService;

    @Autowired
    public AlProducerController(AlProducerService alProducerServiceImpl) {
        this.alProducerService = alProducerServiceImpl;
    }

    @Operation(summary = "Post AuditLogs as Event to EventHub as a Map Input data. i,e; {appName: App1, infoType: InfoA, auditObj: {any obj with respect to InfoType} }")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Saved AuditLogDataContainer", content = {
                    @Content(mediaType = "application/json", schema = @Schema(implementation = AlDataContainer.class))}),
            @ApiResponse(responseCode = "400", description = "AuditLogEventData invalid", content = @Content),
            @ApiResponse(responseCode = "404", description = "AuditLogDataContainer not saved", content = @Content) })
    @PostMapping(value = "/auditLogData/event", produces = MediaType.APPLICATION_JSON_VALUE)
    public String produceNewAuditLog(
            @RequestBody Map<String, Object> auditDataMap) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        AlEventData alDataFromInput = mapper.readValue(mapper.writeValueAsString(auditDataMap), AlEventData.class);

        alProducerService.produceAuditLog(alDataFromInput);
        return "AuditLog Event Produced";
    }

    private void checkMtd() {
        new EventHubClientBuilder().buildAsyncProducerClient().
    }

}
