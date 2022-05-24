package com.jnj.auditlog.consumer.cosmos.service;

import com.azure.spring.data.cosmos.core.query.CosmosPageRequest;
import com.jnj.auditlog.common.AlUtils;
import com.jnj.auditlog.common.model.AlEventData;
import com.jnj.auditlog.consumer.cosmos.AlDataContainer;
import com.jnj.auditlog.consumer.cosmos.repo.AlCosmosDao;
import com.jnj.auditlog.consumer.cosmos.repo.AlCosmosRepository;
import com.jnj.auditlog.consumer.cosmos.repo.AlReactiveRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class AlCosmosServiceImpl implements AlCosmosService {

    private static final Logger logger = LoggerFactory.getLogger(AlCosmosServiceImpl.class);

    private AlCosmosRepository alCosmosRepository;

    private AlReactiveRepository alReactiveRepository;

    @Autowired
    private AlCosmosDao alCosmosDao;

    @Autowired
    public AlCosmosServiceImpl(AlCosmosRepository alCosmosRepository, AlReactiveRepository alReactiveRepository) {
        this.alCosmosRepository = alCosmosRepository;
        this.alReactiveRepository = alReactiveRepository;
    }


    public List<AlDataContainer> getAlByAppNameByPage(String appName, int pageIndex, int pageSize) {
        final Pageable pageRequest = new CosmosPageRequest(pageIndex, pageSize, null);
        Slice<AlDataContainer> auditLogs = alCosmosRepository
                .findByAppName(appName, pageRequest);
        List<AlDataContainer>result = new ArrayList<>();
        result.addAll(auditLogs.getContent());
        while(auditLogs.hasNext()){
            Pageable nextPageable = auditLogs.nextPageable();
            auditLogs = alCosmosRepository.findByAppName(appName, nextPageable);
            result.addAll(auditLogs.getContent());
        }
        return result;
    }

    @Override
    public void deleteAllItems() {
        alCosmosRepository.deleteAll();
    }

    @Override
    public void deleteContainer() throws Exception {
        alCosmosDao.deleteAContainer();
    }

    @Override
    public Mono<AlEventData> saveAlEventData(Mono<AlEventData> alEventData) {
        return alEventData.map(AlUtils::entityToContainer)
                        .flatMap(alReactiveRepository::save)
                        .map(AlUtils::containerToEntity);
    }

    @Override
    public List<AlEventData> findAllAlByAppName(String appName) {

        logger.info("AuditLog by appName : {} ", appName);
        Iterator<AlDataContainer> alIterator = alCosmosRepository.findByAppName(appName).iterator();

        AlDataContainer latestAl = null;
        List<AlEventData> auEventDataList = new ArrayList<>();
        AlEventData auEventData = null;

        while (alIterator.hasNext()) {
            latestAl = alIterator.next();
            auEventData = new AlEventData();
            BeanUtils.copyProperties(latestAl, auEventData);
            auEventDataList.add(auEventData);
        }
        logger.info("AuditLog by appName : {} ", latestAl);
        return auEventDataList;
    }

    @Override
    public AlDataContainer findLatestByAppName(String appName) {
        Iterator<AlDataContainer> alIterator = alCosmosRepository.findByAppName(appName).iterator();
        AlDataContainer latestAl = null;
        while (alIterator.hasNext()) {
            latestAl = alIterator.next();
        }
        return latestAl;
    }

    @Override
    public List<AlEventData> getAlByAppNameAndPath(String appName, Map<String, Object> nestedJsonPathMap) {
        return alCosmosDao.getAlByAppNameAndPath(appName, nestedJsonPathMap);
    }

    @Override
    public Flux<AlEventData> getAlByAppNameAndPath_Stream(String appName, Map<String, Object> nestedJsonPathMap) {
        return alCosmosDao.getAlByAppNameAndPath_Stream(appName, nestedJsonPathMap);
    }

    @Override
    public List<AlEventData> getAllData() {
        List<AlEventData> alEventData = new ArrayList<>();
        alCosmosRepository.findAll().forEach(alLog -> alEventData.add(AlUtils.containerToEntity(alLog)));
        return alEventData;
    }

    @Override
    public Flux<AlEventData> getAllDataStream() {
        return alReactiveRepository.findAll().delayElements(Duration.ofSeconds(1)).map(AlUtils::containerToEntity).log();
    }
}
