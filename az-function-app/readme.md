### Code Generate
#### maven command
````shell
mvn archetype:generate --batch-mode -DarchetypeGroupId=com.microsoft.azure -DarchetypeArtifactId=azure-functions-archetype -DappName=azrarf-functionapp-data-audit-service -DresourceGroup=TEST_RES_GROUP -DappRegion=US_EAST -DgroupId=com.az.func.sample -DartifactId=audit-functions
````
#### Build & Deploy

```shell
mvn clean package
mvn azure-functions:run
mvn azure-functions:deploy
mvn clean package azure-functions:deploy
```
- azure-functions:deploy is not deploying. Deploy through Intellij Azure Explorer

### Command to set properties in appSettings.json
````shell
az functionapp config appsettings set --name azrarf-functionapp-data-audit-service --resource-group AZR-ARF-DMT-DEV --settings "AzureWebJobsStorage=DefaultEndpointsProtocol=https;AccountName=azrarfdmtdevstorage;AccountKey=JDF7aGyJE4Kv+FvyxRK96/CqaOP3B/gNKwjHgG0B5YvDPwmrOVVVd2f/qmkupOiaj0/p8BlpbQ30dAgy2nDN6w==;EndpointSuffix=core.windows.net" "EventHubConnectionString=Endpoint=sb://data-audit-event-dev.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=Ozb2H1gSck+IaJ4ef/HH7w/JO2MiSY+p4vAE7g7sZyQ="  "CosmosDBConnectionString=AccountEndpoint=https://data-audit-dev.documents.azure.com:443/;AccountKey=jgnUDcrXnUQakBfPFh9QLGJSsq7jM2p7KfhYj2sjh7Dwhu1hzFVMOZWTm5N1xDiNvtFCB0mXSYDgS90eW8W1Bw==" "FUNCTIONS_WORKER_RUNTIME=java"
````