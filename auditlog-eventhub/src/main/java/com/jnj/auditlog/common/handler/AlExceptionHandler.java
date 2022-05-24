package com.jnj.auditlog.common.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jnj.auditlog.consumer.cosmos.controller.CosmosDbController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

//@ControllerAdvice
@RestControllerAdvice
public class AlExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(AlExceptionHandler.class);

    @ExceptionHandler(value
            = { JsonProcessingException.class, AlDataException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    protected String handleException(
            RuntimeException ex, WebRequest request) {
        logger.error(ex.getMessage(), ex);
        String bodyOfResponse = "Error while processing Request";
        return bodyOfResponse;
    }

}
