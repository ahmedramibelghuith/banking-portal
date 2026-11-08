package com.Project.GestionBanque.controllers;

import java.util.Map;
import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(ResponseStatusException.class)
  ResponseEntity<?> status(ResponseStatusException ex) {
    return ResponseEntity.status(ex.getStatusCode())
        .body(Map.of("message", ex.getReason() == null ? "Request failed" : ex.getReason()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<?> validation(MethodArgumentNotValidException ex) {
    var errors = ex.getBindingResult().getFieldErrors();
    if (errors.isEmpty())
      return ResponseEntity.badRequest().body(Map.of("message", "Invalid request"));
    var error = errors.getFirst();
    return ResponseEntity.badRequest()
        .body(Map.of("message", error.getField() + ": " + error.getDefaultMessage()));
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  ResponseEntity<?> malformed() {
    return ResponseEntity.badRequest().body(Map.of("message", "Invalid request body"));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<?> duplicate() {
    return ResponseEntity.status(409)
        .body(Map.of("message", "This record already exists or conflicts with existing data"));
  }

  @ExceptionHandler({
    PessimisticLockingFailureException.class,
    OptimisticLockingFailureException.class
  })
  ResponseEntity<?> concurrent() {
    return ResponseEntity.status(409)
        .body(Map.of("message", "The account changed. Refresh and try again"));
  }
}
