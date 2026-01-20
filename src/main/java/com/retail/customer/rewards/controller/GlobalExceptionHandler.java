package com.retail.customer.rewards.controller;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .toList();

        return ResponseEntity.badRequest().body(Map.of("errors", errors));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgNotValid(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> {
                    String[] codes = fe.getCodes();
                    boolean typeMismatch = false;
                    if (codes != null) {
                        for (String c : codes) {
                            if (c != null && c.startsWith("typeMismatch")) {
                                typeMismatch = true;
                                break;
                            }
                        }
                    }

                    if (typeMismatch) {
                        Object rejected = fe.getRejectedValue();
                        String val = rejected == null ? "null" : rejected.toString();
                        return fe.getField() + ": Invalid date format or invalid date '" + val + "'. Expected format yyyy-MM-dd";
                    }

                    return fe.getField() + ": " + fe.getDefaultMessage();
                })
                .toList();

        return ResponseEntity.badRequest().body(Map.of("errors", errors));
    }
}