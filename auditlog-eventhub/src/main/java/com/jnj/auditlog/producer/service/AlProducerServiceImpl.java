package com.jnj.auditlog.producer.service;

import com.jnj.auditlog.consumer.cosmos.AlDataContainer;
import com.jnj.auditlog.consumer.cosmos.repo.AlReactiveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AlProducerServiceImpl implements AlProducerService {

    private AlReactiveRepository alReactiveRepository;

    @Autowired
    public AlProducerServiceImpl(AlReactiveRepository alReactiveRepository) {
        this.alReactiveRepository = alReactiveRepository;
    }

    @Override
    public Mono<AlDataContainer> saveAuditLog(AlDataContainer alDataFromInput) {
        Mono<AlDataContainer> saveAl = alReactiveRepository.save(alDataFromInput);
        return saveAl;
    }
}
