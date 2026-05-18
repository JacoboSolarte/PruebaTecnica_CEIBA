package com.turismo.alquiler.dto;

import com.turismo.alquiler.enums.TipoBicicleta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearBicicletaRequest {

    @NotBlank(message = "El código es requerido")
    private String codigo;

    @NotNull(message = "El tipo de bicicleta es requerido")
    private TipoBicicleta tipo;
}
