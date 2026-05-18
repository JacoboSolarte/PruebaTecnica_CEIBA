package com.turismo.alquiler.dto;

import com.turismo.alquiler.enums.EstadoAlquiler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlquilerDTO {
    private Long id;
    private String codigoBicicleta;
    private String cliente;
    private LocalDateTime horaInicio;
    private LocalDateTime horaFinEstimada;
    private LocalDateTime horaFinReal;
    private double duracionRealHoras;
    private BigDecimal costoBase;
    private BigDecimal multa;
    private BigDecimal costoTotal;
    private EstadoAlquiler estado;
    private LocalDateTime fechaCreacion;
}
