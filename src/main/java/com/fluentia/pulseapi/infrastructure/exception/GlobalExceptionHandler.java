package com.fluentia.pulseapi.infrastructure.exception;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiError> handleApiException(ApiException ex, HttpServletRequest request) {
    return buildResponse(ex.getStatus(), ex.getMessage(), request.getRequestURI(), List.of());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
    List<ApiError.FieldViolation> violations = ex.getBindingResult().getFieldErrors()
        .stream()
        .map(this::toViolation)
        .collect(Collectors.toList());

    return buildResponse(HttpStatus.BAD_REQUEST, "Validation error", request.getRequestURI(), violations);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
    return buildResponse(HttpStatus.UNAUTHORIZED, "Não autenticado", request.getRequestURI(), List.of());
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
    return buildResponse(HttpStatus.FORBIDDEN, "Acesso negado", request.getRequestURI(), List.of());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleUnhandled(Exception ex, HttpServletRequest request) {
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno", request.getRequestURI(), List.of());
  }

  private ApiError.FieldViolation toViolation(FieldError error) {
    return new ApiError.FieldViolation(error.getField(), error.getDefaultMessage());
  }

  private ResponseEntity<ApiError> buildResponse(HttpStatus status, String message, String path,
      List<ApiError.FieldViolation> violations) {
    ApiError body = new ApiError(OffsetDateTime.now(), status.value(), status.getReasonPhrase(), message, path,
        violations);
    return ResponseEntity.status(status).body(body);
  }
}
