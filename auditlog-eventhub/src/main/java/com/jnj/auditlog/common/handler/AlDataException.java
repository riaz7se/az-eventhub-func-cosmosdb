package com.jnj.auditlog.common.handler;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class AlDataException extends RuntimeException {
    public AlDataException() {
        super();
    }
    public AlDataException(String message, Throwable cause) {
        super(message, cause);
    }
    public AlDataException(String message) {
        super(message);
    }
    public AlDataException(Throwable cause) {
        super(cause);
    }
}