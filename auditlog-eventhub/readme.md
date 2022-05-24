# Audit Log Event Stream Data
### Run as Spring Boot or add as dependency in client

## CosmosDbController
- It has endpoints to save auditlog data directly to Cosmos

## AlProducerConttroller
- It send Auditlog Event to EventHub which gets saved to Cosmos DB through Azure Function
