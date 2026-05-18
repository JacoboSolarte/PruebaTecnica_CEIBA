package com.turismo.alquiler.controller;

import com.turismo.alquiler.dto.AlquilerDTO;
import com.turismo.alquiler.dto.CrearAlquilerRequest;
import com.turismo.alquiler.dto.FinalizarAlquilerRequest;
import com.turismo.alquiler.service.AlquilerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alquileres")
@CrossOrigin(origins = "*")
public class AlquilerController {

    @Autowired
    private AlquilerService alquilerService;

    @PostMapping("/iniciar")
    public ResponseEntity<AlquilerDTO> iniciar(@RequestBody @Valid CrearAlquilerRequest request) {
        AlquilerDTO alquiler = alquilerService.iniciarAlquiler(
            request.getCodigoBicicleta(),
            request.getCliente(),
            request.getHoraInicio(),
            request.getDuracionEstimadaHoras()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(alquiler);
    }

    @PostMapping("/{id}/finalizar")
    public ResponseEntity<AlquilerDTO> finalizar(@PathVariable Long id,
        @RequestBody @Valid FinalizarAlquilerRequest request) {
        AlquilerDTO alquiler = alquilerService.finalizarAlquiler(id, request.getHoraFinReal());
        return ResponseEntity.ok(alquiler);
    }

    @GetMapping("/bicicleta/{codigo}")
    public ResponseEntity<List<AlquilerDTO>> obtenerHistorial(@PathVariable String codigo) {
        return ResponseEntity.ok(alquilerService.obtenerHistorial(codigo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlquilerDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(alquilerService.obtenerPorId(id));
    }
}
