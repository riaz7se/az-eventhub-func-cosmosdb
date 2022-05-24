package com.jnj.auditlog.consumer.cosmos;

import com.jnj.auditlog.common.model.AlEventData;
import com.jnj.auditlog.consumer.AlEventConsumer;
import com.jnj.auditlog.consumer.cosmos.repo.AlCosmosRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;

@Component
public class AlCosmosConsumer {

    public static final Logger LOGGER = LoggerFactory.getLogger(AlEventConsumer.class);

    private AlCosmosRepository alCosmosRepository;

    @Autowired
    public AlCosmosConsumer(AlCosmosRepository alCosmosRepository) {
        this.alCosmosRepository = alCosmosRepository;
    }

    public AlEventData getAuditLogByAppName(String appName) {
        LOGGER.info("AuditLog by appName : {} ", appName);
        Iterator<AlDataContainer> alIterator = alCosmosRepository.findByAppName(appName).iterator();

        AlDataContainer latestAl = null;
        while (alIterator.hasNext()) {
            latestAl = alIterator.next();
        }
        LOGGER.info("AuditLog by appName : {} ", latestAl);
        AlEventData returnAlEventData = new AlEventData();
        BeanUtils.copyProperties(latestAl, returnAlEventData);
        return returnAlEventData;

    }


}
