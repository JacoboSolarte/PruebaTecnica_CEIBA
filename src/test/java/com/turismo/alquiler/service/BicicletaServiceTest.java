package com.turismo.alquiler.service;

import com.turismo.alquiler.dto.BicicletaDTO;
import com.turismo.alquiler.entity.Bicicleta;
import com.turismo.alquiler.enums.EstadoBicicleta;
import com.turismo.alquiler.enums.TipoBicicleta;
import com.turismo.alquiler.exception.BicicletaNotFoundException;
import com.turismo.alquiler.repository.BicicletaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class BicicletaServiceTest {

    @Autowired
    private BicicletaService bicicletaService;

    @Autowired
    private BicicletaRepository bicicletaRepository;

    @BeforeEach
    void limpiar() {
        bicicletaRepository.deleteAll();
    }

    @Test
    void testRegistrarBicicleta() {
        BicicletaDTO resultado = bicicletaService.registrarBicicleta("BIC-100", TipoBicicleta.URBANA);

        assertNotNull(resultado);
        assertEquals("BIC-100", resultado.getCodigo());
        assertEquals(TipoBicicleta.URBANA, resultado.getTipo());
        assertEquals(EstadoBicicleta.DISPONIBLE, resultado.getEstado());
    }

    @Test
    void testRegistrarBicicletaDuplicada() {
        bicicletaService.registrarBicicleta("BIC-101", TipoBicicleta.MONTAÑA);

        assertThrows(IllegalArgumentException.class, () ->
            bicicletaService.registrarBicicleta("BIC-101", TipoBicicleta.URBANA)
        );
    }

    @Test
    void testObtenerPorCodigoExistente() {
        bicicletaService.registrarBicicleta("BIC-101", TipoBicicleta.MONTAÑA);

        Bicicleta resultado = bicicletaService.obtenerPorCodigo("BIC-101");

        assertNotNull(resultado);
        assertEquals("BIC-101", resultado.getCodigo());
    }

    @Test
    void testObtenerPorCodigoNoExistente() {
        assertThrows(BicicletaNotFoundException.class, () ->
            bicicletaService.obtenerPorCodigo("BIC-NO-EXISTE")
        );
    }

    @Test
    void testListarDisponibles() {
        bicicletaService.registrarBicicleta("BIC-201", TipoBicicleta.URBANA);
        bicicletaService.registrarBicicleta("BIC-202", TipoBicicleta.MONTAÑA);

        List<BicicletaDTO> disponibles = bicicletaService.listarDisponibles(null);

        assertEquals(2, disponibles.size());
    }

    @Test
    void testListarDisponiblesPorTipo() {
        bicicletaService.registrarBicicleta("BIC-301", TipoBicicleta.URBANA);
        bicicletaService.registrarBicicleta("BIC-302", TipoBicicleta.MONTAÑA);
        bicicletaService.registrarBicicleta("BIC-303", TipoBicicleta.URBANA);

        List<BicicletaDTO> urbanas = bicicletaService.listarDisponibles(TipoBicicleta.URBANA);

        assertEquals(2, urbanas.size());
    }
}
