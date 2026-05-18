package com.turismo.alquiler.dto;

import com.turismo.alquiler.enums.EstadoBicicleta;
import com.turismo.alquiler.enums.TipoBicicleta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BicicletaDTO {
    private Long id;
    private String codigo;
    private TipoBicicleta tipo;
    private EstadoBicicleta estado;
    private LocalDateTime fechaCreacion;
}
