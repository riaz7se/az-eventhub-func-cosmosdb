package com.jnj.auditlog.common.aspect;

import com.jnj.auditlog.common.AlProperties;
import com.jnj.auditlog.common.annotation.AuditLog;
import com.jnj.auditlog.common.model.AlEventData;
import com.jnj.auditlog.producer.AlEventProducer;
import lombok.SneakyThrows;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.IntStream;

@Aspect
@Component
public class AuditLogAspect {

    public static final Logger LOGGER = LoggerFactory.getLogger(AuditLogAspect.class);

    private AlEventProducer alEventProducer;

    private AlProperties alProperties;

    @Autowired
    public AuditLogAspect(AlEventProducer alEventProducer, AlProperties alProperties) {
        this.alEventProducer = alEventProducer;
        this.alProperties = alProperties;
    }

    @Around(value = "@annotation(com.jnj.auditlog.common.annotation.AuditLog) && execution(* *(..)) )")
    public Object handle(ProceedingJoinPoint joinPoint) throws Throwable {

        var methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method1 = joinPoint.getTarget().getClass().getMethod(methodSignature.getMethod().getName(), methodSignature.getMethod().getParameterTypes());
        Method method = methodSignature.getMethod();
        var annotation = method.getAnnotation(AuditLog.class);

        AlEventData finalAlEventData = prepareLogInfo_FromAnnotation(annotation);
        finalAlEventData.setAppName(alProperties.getAppName());

        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() != null) {
            Object principal = auth.getPrincipal();

            if (principal instanceof UserDetails) {
                finalAlEventData.setUpdateBy(((UserDetails)principal).getUsername());
            } else {
                finalAlEventData.setUpdateBy(String.valueOf(principal));
            }
        } else {

        }

        Object[] methodArgs = joinPoint.getArgs();
        String[] methodParams = ((CodeSignature) joinPoint.getSignature()).getParameterNames();

        if (ObjectUtils.isEmpty(finalAlEventData.getAppName())) {
            //while directly posting audit log through endpoint
            finalAlEventData = (AlEventData) methodArgs[0];
        } else {
            Map<String, Object> payloadMap = new HashMap<>();
            IntStream.range(0, methodArgs.length).forEach(i -> {
                String[] alObjArr = annotation.auditObj();

                if (alObjArr.length > 0) {
                    Arrays.asList(alObjArr).stream().forEach(alObj -> {
                        if (alObj.equalsIgnoreCase(methodParams[i])) {
                            extractFieldArgs(payloadMap, methodArgs[i]);
                        }
                    });
                } else {
                    extractFieldArgs(payloadMap, methodArgs[i]);
                }
            });
            finalAlEventData.setPayload(payloadMap);
        }

        Object returnObj = null;
        try {
            returnObj = joinPoint.proceed(joinPoint.getArgs());
        } catch (Throwable e) {
            LOGGER.error("Exception occurred while in JointPoint {} : {}", method1, e.getMessage());
            if (!alProperties.isExceptionLog()) {
                throw new Throwable(e);
            }
        }
        alEventProducer.produceEvent(finalAlEventData);
        //TODO:propoagate exception logs to client

        return returnObj;
    }

    private void extractFieldArgs(Map<String, Object> payloadMap, Object methodArgs) {
        Class<? extends Object> argObjClass = methodArgs.getClass();
        Field[] argFields = argObjClass.getDeclaredFields();
        Arrays.stream(argFields).forEach(argField -> {
            try {
                argField.setAccessible(true);
                payloadMap.put(argField.getName(), argField.get(methodArgs));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private AlEventData prepareLogInfo_FromAnnotation(AuditLog annotation) {
        AlEventData alEventData = AlEventData.builder().infoType(annotation.infoType()).operation(annotation.operation()).build();
        return alEventData;
    }
}
