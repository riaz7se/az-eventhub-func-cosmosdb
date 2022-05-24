package com.jnj.auditlog.consumer.cosmos.repo;

import com.azure.cosmos.*;
import com.azure.cosmos.models.*;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.jnj.auditlog.common.model.AlEventData;
import com.jnj.auditlog.common.AlUtils;
import com.microsoft.azure.documentdb.*;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.jnj.auditlog.consumer.cosmos.AlDataContainer;
import com.jnj.auditlog.consumer.cosmos.CosmosProperties;
import com.microsoft.azure.documentdb.SqlQuerySpec;
import lombok.SneakyThrows;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.util.*;

@Repository
public class AlCosmosDao {

    protected static Logger logger = LoggerFactory.getLogger(AlCosmosDao.class);


    private String collectionLink;

    private CosmosClient client;

    private DocumentClient documentClient;

    private CosmosDatabase database;

    private CosmosContainer container;

    @Autowired
    private CosmosProperties cosmosProperties;

    @Autowired
    private CosmosClientBuilder cosmosClientBuilder;

    private final CosmosAsyncClient cosmosAsyncClient;

    @Autowired
    public AlCosmosDao(CosmosAsyncClient cosmosAsyncClient) {
        this.cosmosAsyncClient = cosmosAsyncClient;
    }

    /**
     * Need to revisit this init method
     *
     */
    @PostConstruct
    public void init() {
        client = cosmosClientBuilder.buildClient();

        collectionLink = String.format("/dbs/%s/colls/%s", cosmosProperties.getDatabase(), "auditlog");

        documentClient = new DocumentClient(cosmosProperties.getUri(), cosmosProperties.getKey(), null, null);

        //  Create database if not exists
        CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists(cosmosProperties.getDatabase());
        database = client.getDatabase(databaseResponse.getProperties().getId());

        //  Create container if not exists
        CosmosContainerProperties containerProperties =
                new CosmosContainerProperties(cosmosProperties.getContainer(), "/appName");
        // Provision throughput
        ThroughputProperties throughputProperties = ThroughputProperties.createManualThroughput(400);

        //  Create container with 200 RU/s
        CosmosContainerResponse containerResponse = database.createContainerIfNotExists(containerProperties, throughputProperties);
        container = database.getContainer(containerResponse.getProperties().getId());
    }

    // Container delete
    public void deleteAContainer() throws Exception {
        logger.info("Delete container " + cosmosProperties.getContainer() + " by ID.");

        // Delete container
        CosmosContainerResponse containerResp = database.getContainer(cosmosProperties.getContainer()).delete(new CosmosContainerRequestOptions());
        logger.info("Status code for container delete: {}",containerResp.getStatusCode());

        logger.info("Done.");
    }

    private void queryOrderBy(String appName) throws Exception {
        logger.info("ORDER BY queries");

        // Numerical ORDER BY
        executeQueryPrintSingleResult("SELECT * FROM auditLog al WHERE al.appName = '"+appName+"' ORDER BY al.Children[0].Grade");
    }


    private void queryDistinct(String appName) throws Exception {
        logger.info("DISTINCT queries");

        // DISTINCT query
        executeQueryPrintSingleResult("SELECT DISTINCT c.lastName from c");
    }

    @SneakyThrows
    public List<AlEventData> getAlByAppNameAndPath(String appName, Map<String, Object> nestedJsonPathMap) {

//        CosmosPagedIterable<AuditLogDataContainer> alDataList = container.queryItems(sql, new CosmosQueryRequestOptions(), AuditLogDataContainer.class);

        List<com.microsoft.azure.documentdb.SqlParameter> paramList = new ArrayList<com.microsoft.azure.documentdb.SqlParameter>();
        paramList.add(new com.microsoft.azure.documentdb.SqlParameter("@appName", appName));

        List<com.azure.cosmos.models.SqlParameter> cosmosSqlParamList = new ArrayList<>();
        cosmosSqlParamList.add(new com.azure.cosmos.models.SqlParameter("@appName", appName));

        StringBuilder nestedPathSql = new StringBuilder();


        AlEventData alSearchByData = new AlEventData();
//        if (!ObjectUtils.isEmpty(alSearchByData.getOperation())) {
//            nestedPathSql.append(" and c.operation = @operation");
//            paramList.add(new com.microsoft.azure.documentdb.SqlParameter("@"+jsonStr, Integer.parseInt(String.valueOf(val))));
//        }


        nestedJsonPathMap.forEach((jsonStr, val) -> {
            if (NumberUtils.isDigits(String.valueOf(val))) {
                nestedPathSql.append(" and c."+jsonStr+" = @"+jsonStr);
                paramList.add(new com.microsoft.azure.documentdb.SqlParameter("@"+jsonStr, Integer.parseInt(String.valueOf(val))));
                cosmosSqlParamList.add(new com.azure.cosmos.models.SqlParameter("@"+jsonStr, Integer.parseInt(String.valueOf(val))));
            } else if (val instanceof Map<?, ?> collectionVal) {
                collectionVal.forEach((pKey, pVal) -> {
                    nestedPathSql.append(" and c.payload."+pKey+" = @"+pKey);
                    if (NumberUtils.isDigits(String.valueOf(pVal))) {
                        paramList.add(new com.microsoft.azure.documentdb.SqlParameter("@"+pKey, Integer.parseInt(String.valueOf(pVal))));
                        cosmosSqlParamList.add(new com.azure.cosmos.models.SqlParameter("@"+pKey, Integer.parseInt(String.valueOf(pVal))));
                    } else {
                        paramList.add(new com.microsoft.azure.documentdb.SqlParameter("@"+pKey, pVal));
                        cosmosSqlParamList.add(new com.azure.cosmos.models.SqlParameter("@"+pKey, pVal));
                    }
                });
            } else {
                nestedPathSql.append(" and c."+jsonStr+" = @"+jsonStr);
                paramList.add(new com.microsoft.azure.documentdb.SqlParameter("@"+jsonStr, val));
                cosmosSqlParamList.add(new com.azure.cosmos.models.SqlParameter("@"+jsonStr, val));
            }
        });

        SqlQuerySpec querySpec = new SqlQuerySpec(
                "SELECT * FROM c WHERE (c.appName = @appName "+nestedPathSql+")",
                new SqlParameterCollection(paramList));

        FeedOptions options = new FeedOptions();
//        options.setEnableCrossPartitionQuery(true);

        //for Non
        Iterator<Document> it = documentClient.queryDocuments(collectionLink, querySpec, options).getQueryIterator();


        Flux<AlEventData> map = this.cosmosAsyncClient.getDatabase(cosmosProperties.getDatabase()).getContainer(cosmosProperties.getContainer())
                .queryItems(new com.azure.cosmos.models.SqlQuerySpec("SELECT * FROM c WHERE (c.appName = @appName " + nestedPathSql + ")", cosmosSqlParamList), AlDataContainer.class)
                .map(AlUtils::containerToEntity).log();



        List<AlEventData> alList = new ArrayList<>();
        while(it.hasNext()) {
            alList.add(AlUtils.documentToEntity(it.next()));
        }
        return alList;
    }

    @SneakyThrows
    public Flux<AlEventData> getAlByAppNameAndPath_Stream(String appName, Map<String, Object> nestedJsonPathMap) {
        List<com.azure.cosmos.models.SqlParameter> cosmosSqlParamList = new ArrayList<>();
        cosmosSqlParamList.add(new com.azure.cosmos.models.SqlParameter("@appName", appName));

        StringBuilder nestedPathSql = new StringBuilder();


        nestedJsonPathMap.forEach((jsonStr, val) -> {
            if (NumberUtils.isDigits(String.valueOf(val))) {
                nestedPathSql.append(" and c."+jsonStr+" = @"+jsonStr);
                cosmosSqlParamList.add(new com.azure.cosmos.models.SqlParameter("@"+jsonStr, Integer.parseInt(String.valueOf(val))));
            } else if (val instanceof Map<?, ?> collectionVal) {
                collectionVal.forEach((pKey, pVal) -> {
                    nestedPathSql.append(" and c.payload."+pKey+" = @"+pKey);
                    if (NumberUtils.isDigits(String.valueOf(pVal))) {
                        cosmosSqlParamList.add(new com.azure.cosmos.models.SqlParameter("@"+pKey, Integer.parseInt(String.valueOf(pVal))));
                    } else {
                        cosmosSqlParamList.add(new com.azure.cosmos.models.SqlParameter("@"+pKey, pVal));
                    }
                });
            } else {
                nestedPathSql.append(" and c."+jsonStr+" = @"+jsonStr);
                cosmosSqlParamList.add(new com.azure.cosmos.models.SqlParameter("@"+jsonStr, val));
            }
        });

        FeedOptions options = new FeedOptions();
//        options.setEnableCrossPartitionQuery(true);


        Flux<AlEventData> returnFlux = this.cosmosAsyncClient.getDatabase(cosmosProperties.getDatabase()).getContainer(cosmosProperties.getContainer())
                .queryItems(new com.azure.cosmos.models.SqlQuerySpec("SELECT * FROM c WHERE (c.appName = @appName " + nestedPathSql + ")", cosmosSqlParamList), AlDataContainer.class)
                .map(AlUtils::containerToEntity).log();
        return returnFlux;
    }

    // Document read
    private void readDocumentById() throws Exception {
        String documentId = "auditlog";
        logger.info("Read document " + documentId + " by ID.");

        //  Read document by ID
        AlDataContainer al = container.readItem(documentId, new PartitionKey("/appName"), AlDataContainer.class).getItem();

        // Check result
        logger.info("Al: " + al.getAuditId() + " with partition key " + al.getAppName());

        logger.info("Done.");
    }

    private void executeQueryPrintSingleResult(String sql) {
        logger.info("Execute query {}",sql);
        client = cosmosClientBuilder.buildClient();
        CosmosPagedIterable<AlDataContainer> alDataList = container.queryItems(sql, new CosmosQueryRequestOptions(), AlDataContainer.class);

        if (alDataList.iterator().hasNext()) {
            AlDataContainer alDataContainer = alDataList.iterator().next();
            logger.info(String.format("First query result: AuditLog with (/id, partition key) = (%s,%s)",alDataContainer.getAuditId(), alDataContainer.getAppName()));
        }
        logger.info("Done.");
    }
}
