package com.turismo.alquiler.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.turismo.alquiler.dto.CrearAlquilerRequest;
import com.turismo.alquiler.dto.CrearBicicletaRequest;
import com.turismo.alquiler.dto.FinalizarAlquilerRequest;
import com.turismo.alquiler.enums.TipoBicicleta;
import com.turismo.alquiler.repository.AlquilerRepository;
import com.turismo.alquiler.repository.BicicletaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AlquilerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AlquilerRepository alquilerRepository;

    @Autowired
    private BicicletaRepository bicicletaRepository;

    @BeforeEach
    void setup() {
        alquilerRepository.deleteAll();
        bicicletaRepository.deleteAll();
    }

    @Test
    void testFlujoCompletoAlquiler() throws Exception {
        // 1. Registrar bicicleta
        CrearBicicletaRequest crearBicicleta = new CrearBicicletaRequest("BIC-INT-001", TipoBicicleta.URBANA);
        mockMvc.perform(post("/api/bicicletas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(crearBicicleta)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.codigo").value("BIC-INT-001"))
            .andExpect(jsonPath("$.estado").value("DISPONIBLE"));

        // 2. Iniciar alquiler
        CrearAlquilerRequest iniciarRequest = new CrearAlquilerRequest(
            "BIC-INT-001", "Cliente Test",
            LocalDateTime.of(2026, 4, 28, 10, 0), 2
        );
        MvcResult result = mockMvc.perform(post("/api/alquileres/iniciar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(iniciarRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.cliente").value("Cliente Test"))
            .andExpect(jsonPath("$.estado").value("EN_CURSO"))
            .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Long alquilerId = objectMapper.readTree(responseBody).get("id").asLong();

        // 3. Verificar que la bicicleta está alquilada
        mockMvc.perform(get("/api/bicicletas/BIC-INT-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("ALQUILADA"));

        // 4. Finalizar alquiler (a tiempo)
        FinalizarAlquilerRequest finalizarRequest = new FinalizarAlquilerRequest(
            LocalDateTime.of(2026, 4, 28, 12, 0)
        );
        mockMvc.perform(post("/api/alquileres/" + alquilerId + "/finalizar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(finalizarRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.costoBase").value(7000))
            .andExpect(jsonPath("$.multa").value(0))
            .andExpect(jsonPath("$.costoTotal").value(7000))
            .andExpect(jsonPath("$.estado").value("FINALIZADO"));

        // 5. Verificar que la bicicleta volvió a estar disponible
        mockMvc.perform(get("/api/bicicletas/BIC-INT-001"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("DISPONIBLE"));
    }

    @Test
    void testAlquilerConMulta() throws Exception {
        CrearBicicletaRequest crearBicicleta = new CrearBicicletaRequest("BIC-INT-002", TipoBicicleta.MONTAÑA);
        mockMvc.perform(post("/api/bicicletas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(crearBicicleta)))
            .andExpect(status().isCreated());

        CrearAlquilerRequest iniciarRequest = new CrearAlquilerRequest(
            "BIC-INT-002", "Cliente Tardío",
            LocalDateTime.of(2026, 4, 28, 10, 0), 2
        );
        MvcResult result = mockMvc.perform(post("/api/alquileres/iniciar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(iniciarRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        Long alquilerId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        FinalizarAlquilerRequest finalizarRequest = new FinalizarAlquilerRequest(
            LocalDateTime.of(2026, 4, 28, 13, 20) // 3h 20min después
        );
        mockMvc.perform(post("/api/alquileres/" + alquilerId + "/finalizar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(finalizarRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.costoBase").value(20000))
            .andExpect(jsonPath("$.multa").value(5000))
            .andExpect(jsonPath("$.costoTotal").value(25000));
    }

    @Test
    void testBicicletaNoDisponible() throws Exception {
        CrearBicicletaRequest crearBicicleta = new CrearBicicletaRequest("BIC-INT-003", TipoBicicleta.URBANA);
        mockMvc.perform(post("/api/bicicletas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(crearBicicleta)))
            .andExpect(status().isCreated());

        CrearAlquilerRequest iniciarRequest = new CrearAlquilerRequest(
            "BIC-INT-003", "Cliente 1", LocalDateTime.now(), 2
        );
        mockMvc.perform(post("/api/alquileres/iniciar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(iniciarRequest)))
            .andExpect(status().isCreated());

        // Intentar alquilar de nuevo
        CrearAlquilerRequest segundoRequest = new CrearAlquilerRequest(
            "BIC-INT-003", "Cliente 2", LocalDateTime.now(), 1
        );
        mockMvc.perform(post("/api/alquileres/iniciar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(segundoRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testListarBicicletasDisponibles() throws Exception {
        mockMvc.perform(post("/api/bicicletas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CrearBicicletaRequest("BIC-D1", TipoBicicleta.URBANA))))
            .andExpect(status().isCreated());
        mockMvc.perform(post("/api/bicicletas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CrearBicicletaRequest("BIC-D2", TipoBicicleta.MONTAÑA))))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/bicicletas/disponibles"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/bicicletas/disponibles?tipo=URBANA"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void testHistorialAlquileres() throws Exception {
        mockMvc.perform(post("/api/bicicletas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CrearBicicletaRequest("BIC-H1", TipoBicicleta.URBANA))))
            .andExpect(status().isCreated());

        CrearAlquilerRequest iniciarRequest = new CrearAlquilerRequest(
            "BIC-H1", "Cliente Historial", LocalDateTime.now(), 1
        );
        mockMvc.perform(post("/api/alquileres/iniciar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(iniciarRequest)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/alquileres/bicicleta/BIC-H1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }
}
