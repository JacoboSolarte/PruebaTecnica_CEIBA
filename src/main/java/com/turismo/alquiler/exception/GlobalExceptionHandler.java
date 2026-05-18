package com.turismo.alquiler.exception;

import com.turismo.alquiler.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BicicletaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBicicletaNotFound(BicicletaNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(404, e.getMessage()));
    }

    @ExceptionHandler(BicicletaNoDisponibleException.class)
    public ResponseEntity<ErrorResponse> handleBicicletaNoDisponible(BicicletaNoDisponibleException e) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(400, e.getMessage()));
    }

    @ExceptionHandler(AlquilerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAlquilerNotFound(AlquilerNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(404, e.getMessage()));
    }

    @ExceptionHandler(AlquilerYaFinalizadoException.class)
    public ResponseEntity<ErrorResponse> handleAlquilerYaFinalizado(AlquilerYaFinalizadoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(409, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException e) {
        String mensaje = e.getBindingResult().getAllErrors().stream()
            .map(error -> error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(400, "Error de validación: " + mensaje));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(400, e.getMessage()));
    }
}
