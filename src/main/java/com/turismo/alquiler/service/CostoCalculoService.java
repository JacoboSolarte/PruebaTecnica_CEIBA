package com.turismo.alquiler.service;

import com.turismo.alquiler.enums.TipoBicicleta;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class CostoCalculoService {

    /**
     * RN-02: tiempo real redondeado al alza × tarifa por hora
     */
    public BigDecimal calcularCostoBase(LocalDateTime horaInicio, LocalDateTime horaFinReal,
                                        TipoBicicleta tipo) {
        double duracionHoras = Duration.between(horaInicio, horaFinReal).toMinutes() / 60.0;
        long horasRedondeadas = (long) Math.ceil(duracionHoras);
        return BigDecimal.valueOf(horasRedondeadas * tipo.getTarifaPorHora());
    }

    /**
     * RN-03: 50% de tarifa × horas de retraso (redondeadas al alza)
     */
    public BigDecimal calcularMulta(LocalDateTime horaFinEstimada, LocalDateTime horaFinReal,
                                    TipoBicicleta tipo) {
        if (!horaFinReal.isAfter(horaFinEstimada)) {
            return BigDecimal.ZERO;
        }

        double minutosRetraso = Duration.between(horaFinEstimada, horaFinReal).toMinutes();
        long horasRetraso = (long) Math.ceil(minutosRetraso / 60.0);
        long multaPorHora = (long) (tipo.getTarifaPorHora() * 0.5);

        return BigDecimal.valueOf(horasRetraso * multaPorHora);
    }

    public BigDecimal calcularCostoTotal(BigDecimal costoBase, BigDecimal multa) {
        return costoBase.add(multa != null ? multa : BigDecimal.ZERO);
    }
}
