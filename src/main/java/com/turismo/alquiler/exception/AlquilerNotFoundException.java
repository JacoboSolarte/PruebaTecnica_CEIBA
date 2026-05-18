package com.turismo.alquiler.exception;

public class AlquilerNotFoundException extends RuntimeException {
    public AlquilerNotFoundException(String message) {
        super(message);
    }
}
