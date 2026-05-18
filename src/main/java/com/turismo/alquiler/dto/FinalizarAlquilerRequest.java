package com.turismo.alquiler.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinalizarAlquilerRequest {

    @NotNull(message = "La hora de fin real es requerida")
    private LocalDateTime horaFinReal;
}
