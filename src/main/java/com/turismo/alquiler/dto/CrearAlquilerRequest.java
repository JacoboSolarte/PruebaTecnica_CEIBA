package com.turismo.alquiler.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearAlquilerRequest {

    @NotBlank(message = "El código de la bicicleta es requerido")
    private String codigoBicicleta;

    @NotBlank(message = "El nombre del cliente es requerido")
    private String cliente;

    @NotNull(message = "La hora de inicio es requerida")
    private LocalDateTime horaInicio;

    @Min(value = 1, message = "La duración estimada debe ser mayor a 0")
    private int duracionEstimadaHoras;
}
