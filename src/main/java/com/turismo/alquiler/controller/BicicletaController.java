package com.turismo.alquiler.controller;

import com.turismo.alquiler.dto.BicicletaDTO;
import com.turismo.alquiler.dto.CrearBicicletaRequest;
import com.turismo.alquiler.entity.Bicicleta;
import com.turismo.alquiler.enums.TipoBicicleta;
import com.turismo.alquiler.service.BicicletaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bicicletas")
@CrossOrigin(origins = "*")
public class BicicletaController {

    @Autowired
    private BicicletaService bicicletaService;

    @PostMapping
    public ResponseEntity<BicicletaDTO> registrar(@RequestBody @Valid CrearBicicletaRequest request) {
        BicicletaDTO bicicleta = bicicletaService.registrarBicicleta(request.getCodigo(), request.getTipo());
        return ResponseEntity.status(HttpStatus.CREATED).body(bicicleta);
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<BicicletaDTO> obtenerPorCodigo(@PathVariable String codigo) {
        Bicicleta bicicleta = bicicletaService.obtenerPorCodigo(codigo);
        return ResponseEntity.ok(bicicletaService.convertirADTO(bicicleta));
    }

    @GetMapping
    public ResponseEntity<List<BicicletaDTO>> listarTodas() {
        return ResponseEntity.ok(bicicletaService.listarTodas());
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<BicicletaDTO>> listarDisponibles(
            @RequestParam(required = false) TipoBicicleta tipo) {
        return ResponseEntity.ok(bicicletaService.listarDisponibles(tipo));
    }
}
