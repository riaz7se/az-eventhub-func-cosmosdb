package com.az.func.sample;

import com.microsoft.azure.functions.OutputBinding;
import com.microsoft.azure.functions.annotation.Cardinality;
import com.microsoft.azure.functions.annotation.CosmosDBOutput;
import com.microsoft.azure.functions.annotation.EventHubOutput;
import com.microsoft.azure.functions.annotation.EventHubTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import com.microsoft.azure.functions.ExecutionContext;

import java.util.HashMap;

public class Function {

//
//    @FunctionName("generateAuditLogData")
//    @EventHubOutput(
//            name = "eventHubOutput",
//            eventHubName = "data-audit-event",
//            connection = "EventHubConnectionString")
//    public AuditLogEventData generateAuditLogData(@TimerTrigger(name = "timerInfo", schedule = "*/10 * * * * *") // every 10 seconds
//                                                  String timerInfo, final ExecutionContext context) {
//
//        context.getLogger().info("Java Timer trigger function executed at: "
//                + java.time.LocalDateTime.now());
//        double random = Math.random() * 10;
//
//        String operation = random % 2 == 0 ? "CREATE" : "UPDATE";
//
//        return AuditLogEventData.builder()
//                .appName("APP-" + random).infoType("INFO-" + random)
//                .operation("CREATE-" + random)
//                .auditId(Double.toString(random * 1223)).requestId("r-" + random).payload(new HashMap<>())
//                .updateTimestamp(java.time.LocalDateTime.now().toString()).build();
//    }
//

    @FunctionName("azrarf-functionapp-data-audit-service")
    public void processAuditLogData(
            @EventHubTrigger(
                    name = "eventHubInput",
                    eventHubName = "data-audit-event", // blank because the value is included in the connection string
                    cardinality = Cardinality.ONE,
                    connection = "EventHubConnectionString")
            Object auditLogEventData,
            @CosmosDBOutput(
                    name = "auditLogOutput",
                    databaseName = "auditlogDB",
                    collectionName = "auditlog",
                    connectionStringSetting = "CosmosDBConnectionString")
            OutputBinding<Object> document,
            final ExecutionContext context) {

        context.getLogger().info("Event hub message received: " + auditLogEventData.toString());

        //enrich or modify auditLogEventData here

        document.setValue(auditLogEventData);
    }
}
