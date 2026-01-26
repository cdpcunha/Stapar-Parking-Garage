package com.stapar.parking.garage.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        return constructErrorResponse(ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity() {
        return constructErrorResponse(HttpStatus.CONFLICT, "Conflito de integridade de dados: possivelmente um registro duplicado.");
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound() {
        return constructErrorResponse(HttpStatus.NOT_FOUND, "O recurso solicitado não foi encontrado.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException() {
        return constructErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro interno inesperado.");
    }

    private ResponseEntity<ErrorResponse> constructErrorResponse(HttpStatus status, String detail) {
        log.error(detail);
        ErrorResponse errorResponse = new ErrorResponse(status,detail,Instant.now());
        return ResponseEntity.status(status).body(errorResponse);
    }
}