package com.turismo.alquiler.service;

import com.turismo.alquiler.dto.AlquilerDTO;
import com.turismo.alquiler.entity.Bicicleta;
import com.turismo.alquiler.enums.EstadoBicicleta;
import com.turismo.alquiler.enums.TipoBicicleta;
import com.turismo.alquiler.exception.AlquilerNotFoundException;
import com.turismo.alquiler.exception.AlquilerYaFinalizadoException;
import com.turismo.alquiler.exception.BicicletaNoDisponibleException;
import com.turismo.alquiler.repository.AlquilerRepository;
import com.turismo.alquiler.repository.BicicletaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class AlquilerServiceTest {

    @Autowired
    private AlquilerService alquilerService;

    @Autowired
    private BicicletaService bicicletaService;

    @Autowired
    private BicicletaRepository bicicletaRepository;

    @Autowired
    private AlquilerRepository alquilerRepository;

    @BeforeEach
    void setup() {
        alquilerRepository.deleteAll();
        bicicletaRepository.deleteAll();
        bicicletaService.registrarBicicleta("BIC-001", TipoBicicleta.URBANA);
    }

    @Test
    void testIniciarAlquiler() {
        LocalDateTime inicio = LocalDateTime.now();

        AlquilerDTO alquiler = alquilerService.iniciarAlquiler("BIC-001", "Juan", inicio, 2);

        assertNotNull(alquiler);
        assertEquals("Juan", alquiler.getCliente());
        assertNotNull(alquiler.getId());

        Bicicleta bicicleta = bicicletaService.obtenerPorCodigo("BIC-001");
        assertEquals(EstadoBicicleta.ALQUILADA, bicicleta.getEstado());
    }

    @Test
    void testIniciarAlquilerBicicletaNoDisponible() {
        LocalDateTime inicio = LocalDateTime.now();
        alquilerService.iniciarAlquiler("BIC-001", "Juan", inicio, 2);

        assertThrows(BicicletaNoDisponibleException.class, () ->
            alquilerService.iniciarAlquiler("BIC-001", "Pedro", inicio, 1)
        );
    }

    @Test
    void testFinalizarAlquiler() {
        LocalDateTime inicio = LocalDateTime.of(2026, 4, 28, 10, 0);
        AlquilerDTO alquilerIniciado = alquilerService.iniciarAlquiler("BIC-001", "Juan", inicio, 2);

        LocalDateTime fin = LocalDateTime.of(2026, 4, 28, 12, 0);
        AlquilerDTO alquilerFinalizado = alquilerService.finalizarAlquiler(alquilerIniciado.getId(), fin);

        assertEquals(new BigDecimal(7000), alquilerFinalizado.getCostoBase()); // 2h × $3.500
        assertEquals(BigDecimal.ZERO, alquilerFinalizado.getMulta());
        assertEquals(new BigDecimal(7000), alquilerFinalizado.getCostoTotal());

        Bicicleta bicicleta = bicicletaService.obtenerPorCodigo("BIC-001");
        assertEquals(EstadoBicicleta.DISPONIBLE, bicicleta.getEstado());
    }

    @Test
    void testFinalizarAlquilerConMulta() {
        alquilerRepository.deleteAll();
        bicicletaRepository.deleteAll();
        bicicletaService.registrarBicicleta("BIC-002", TipoBicicleta.MONTAÑA);

        LocalDateTime inicio = LocalDateTime.of(2026, 4, 28, 10, 0);
        AlquilerDTO alquilerIniciado = alquilerService.iniciarAlquiler("BIC-002", "Juan", inicio, 2);

        // Devolver 3h 20min después del inicio
        LocalDateTime fin = LocalDateTime.of(2026, 4, 28, 13, 20);
        AlquilerDTO alquilerFinalizado = alquilerService.finalizarAlquiler(alquilerIniciado.getId(), fin);

        assertEquals(new BigDecimal(20000), alquilerFinalizado.getCostoBase()); // 4h × $5.000
        assertEquals(new BigDecimal(5000), alquilerFinalizado.getMulta()); // 2h retraso × $2.500
        assertEquals(new BigDecimal(25000), alquilerFinalizado.getCostoTotal());
    }

    @Test
    void testFinalizarAlquilerNoExistente() {
        assertThrows(AlquilerNotFoundException.class, () ->
            alquilerService.finalizarAlquiler(999L, LocalDateTime.now())
        );
    }

    @Test
    void testFinalizarAlquilerYaFinalizado() {
        LocalDateTime inicio = LocalDateTime.now();
        AlquilerDTO alquilerIniciado = alquilerService.iniciarAlquiler("BIC-001", "Juan", inicio, 2);

        LocalDateTime fin = LocalDateTime.now().plusHours(2);
        alquilerService.finalizarAlquiler(alquilerIniciado.getId(), fin);

        assertThrows(AlquilerYaFinalizadoException.class, () ->
            alquilerService.finalizarAlquiler(alquilerIniciado.getId(), fin)
        );
    }

    @Test
    void testObtenerHistorial() {
        LocalDateTime inicio = LocalDateTime.now();
        alquilerService.iniciarAlquiler("BIC-001", "Juan", inicio, 2);

        List<AlquilerDTO> historial = alquilerService.obtenerHistorial("BIC-001");

        assertEquals(1, historial.size());
    }
}
