package com.example.shelter.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> business(ResponseStatusException e) {
        return error(e.getStatusCode().value(), e.getReason() == null ? "Запрос отклонён" : e.getReason());
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> forbidden() { return error(403, "Недостаточно прав"); }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> conflict() { return error(409, "Данные конфликтуют с существующей записью"); }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<?> invalid() { return error(400, "Некорректный запрос"); }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> unexpected() { return error(500, "Ошибка сервера"); }
    private ResponseEntity<?> error(int status, String message) {
        return ResponseEntity.status(status).body(Map.of("status", status, "message", message));
    }
}
