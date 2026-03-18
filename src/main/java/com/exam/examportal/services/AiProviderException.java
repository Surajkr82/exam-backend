package com.exam.examportal.services;

import org.springframework.http.HttpStatus;

public class AiProviderException extends RuntimeException {

    private final HttpStatus status;

    public AiProviderException(HttpStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}