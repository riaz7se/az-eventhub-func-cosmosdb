package com.jnj.auditlog.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jnj.auditlog.common.model.AlEventData;
import com.jnj.auditlog.consumer.cosmos.AlDataContainer;
import com.microsoft.azure.documentdb.Document;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AlUtils {

    public static AlEventData containerToEntity(AlDataContainer alDataContainer) {
        AlEventData alEventData = new AlEventData();
        BeanUtils.copyProperties(alDataContainer, alEventData);
        return alEventData;
    }


    public static AlDataContainer entityToContainer(AlEventData alEventData) {
        AlDataContainer alDataContainer = new AlDataContainer();
        BeanUtils.copyProperties(alEventData, alDataContainer);
        return alDataContainer;
    }

    public static AlEventData documentToEntity(Document d) throws JsonProcessingException {
        AlEventData alData = new AlEventData();
        alData.setAppName(d.getString(CosmosConstants.APPNAME));
        Map<String, Object> payloadMap = new ObjectMapper().readValue(JSONObject.valueToString(d.getObject("payload")), Map.class);
        alData.setPayload(payloadMap);
        alData.setInfoType(d.getString(CosmosConstants.INFO_TYPE));
        alData.setOperation(d.getString(CosmosConstants.OPERATION));

        return alData;
    }

    public static boolean isValidateJsonSearchPath(Map<String, Object> searchMap) {
        HashSet<String> validJsonKeys = new HashSet<>() {{
            add("appName");
            add("infoType");
            add("operation");
            add("updateBy");
            add("payload");
        }};
        Set<String> searchKeySet = searchMap.keySet();
        searchKeySet.removeAll(validJsonKeys);
        return searchKeySet.size() > 0 ? false : true;

    }
}
