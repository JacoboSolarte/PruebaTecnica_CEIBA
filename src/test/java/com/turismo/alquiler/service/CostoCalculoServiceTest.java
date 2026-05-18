package com.turismo.alquiler.service;

import com.turismo.alquiler.enums.TipoBicicleta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class CostoCalculoServiceTest {

    @Autowired
    private CostoCalculoService costoCalculoService;

    @Test
    void testCalcularCostoBaseRedondeAlza() {
        // 1h 10min debe redondearse a 2 horas
        LocalDateTime inicio = LocalDateTime.of(2026, 4, 28, 10, 0);
        LocalDateTime fin = LocalDateTime.of(2026, 4, 28, 11, 10);

        BigDecimal costo = costoCalculoService.calcularCostoBase(inicio, fin, TipoBicicleta.URBANA);

        assertEquals(new BigDecimal(7000), costo); // 2 horas × $3.500
    }

    @Test
    void testCalcularCostoBaseExacto() {
        // 2h exactas
        LocalDateTime inicio = LocalDateTime.of(2026, 4, 28, 10, 0);
        LocalDateTime fin = LocalDateTime.of(2026, 4, 28, 12, 0);

        BigDecimal costo = costoCalculoService.calcularCostoBase(inicio, fin, TipoBicicleta.MONTAÑA);

        assertEquals(new BigDecimal(10000), costo); // 2 horas × $5.000
    }

    @Test
    void testCalcularCostoBase3HorasYMedia() {
        // 3h 30min redondeado = 4 horas
        LocalDateTime inicio = LocalDateTime.of(2026, 4, 28, 10, 0);
        LocalDateTime fin = LocalDateTime.of(2026, 4, 28, 13, 30);

        BigDecimal costo = costoCalculoService.calcularCostoBase(inicio, fin, TipoBicicleta.ELÉCTRICA);

        assertEquals(new BigDecimal(30000), costo); // 4 horas × $7.500
    }

    @Test
    void testCalcularMultaPorRetraso() {
        // Estimada 2h, devuelta a las 3h 20min → retraso 1h 20min → ceil = 2h
        LocalDateTime estimada = LocalDateTime.of(2026, 4, 28, 10, 0).plusHours(2);
        LocalDateTime real = LocalDateTime.of(2026, 4, 28, 10, 0).plusHours(3).plusMinutes(20);

        BigDecimal multa = costoCalculoService.calcularMulta(estimada, real, TipoBicicleta.MONTAÑA);

        // 2 horas × ($5.000 × 50%) = $5.000
        assertEquals(new BigDecimal(5000), multa);
    }

    @Test
    void testCalcularMultaSinRetraso() {
        LocalDateTime estimada = LocalDateTime.of(2026, 4, 28, 12, 0);
        LocalDateTime real = LocalDateTime.of(2026, 4, 28, 11, 0);

        BigDecimal multa = costoCalculoService.calcularMulta(estimada, real, TipoBicicleta.URBANA);

        assertEquals(BigDecimal.ZERO, multa);
    }

    @Test
    void testCalcularMultaExactamente() {
        LocalDateTime estimada = LocalDateTime.of(2026, 4, 28, 12, 0);
        LocalDateTime real = LocalDateTime.of(2026, 4, 28, 12, 0);

        BigDecimal multa = costoCalculoService.calcularMulta(estimada, real, TipoBicicleta.URBANA);

        assertEquals(BigDecimal.ZERO, multa);
    }

    @Test
    void testCalcularCostoTotalConMulta() {
        BigDecimal costoBase = new BigDecimal(20000);
        BigDecimal multa = new BigDecimal(5000);

        BigDecimal total = costoCalculoService.calcularCostoTotal(costoBase, multa);

        assertEquals(new BigDecimal(25000), total);
    }

    @Test
    void testCalcularCostoTotalSinMulta() {
        BigDecimal costoBase = new BigDecimal(7000);
        BigDecimal multa = BigDecimal.ZERO;

        BigDecimal total = costoCalculoService.calcularCostoTotal(costoBase, multa);

        assertEquals(new BigDecimal(7000), total);
    }
}
