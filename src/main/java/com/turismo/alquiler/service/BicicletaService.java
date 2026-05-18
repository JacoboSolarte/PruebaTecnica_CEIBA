package com.turismo.alquiler.service;

import com.turismo.alquiler.dto.BicicletaDTO;
import com.turismo.alquiler.entity.Bicicleta;
import com.turismo.alquiler.enums.EstadoBicicleta;
import com.turismo.alquiler.enums.TipoBicicleta;
import com.turismo.alquiler.exception.BicicletaNotFoundException;
import com.turismo.alquiler.repository.BicicletaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BicicletaService {

    @Autowired
    private BicicletaRepository bicicletaRepository;

    public BicicletaDTO registrarBicicleta(String codigo, TipoBicicleta tipo) {
        if (bicicletaRepository.findByCodigo(codigo).isPresent()) {
            throw new IllegalArgumentException("El código " + codigo + " ya existe");
        }

        Bicicleta bicicleta = new Bicicleta();
        bicicleta.setCodigo(codigo);
        bicicleta.setTipo(tipo);
        bicicleta.setEstado(EstadoBicicleta.DISPONIBLE);

        return convertirADTO(bicicletaRepository.save(bicicleta));
    }

    public Bicicleta obtenerPorCodigo(String codigo) {
        return bicicletaRepository.findByCodigo(codigo)
            .orElseThrow(() -> new BicicletaNotFoundException("Bicicleta " + codigo + " no encontrada"));
    }

    public List<BicicletaDTO> listarDisponibles(TipoBicicleta tipo) {
        List<Bicicleta> bicicletas = (tipo != null)
            ? bicicletaRepository.findByEstadoAndTipo(EstadoBicicleta.DISPONIBLE, tipo)
            : bicicletaRepository.findByEstado(EstadoBicicleta.DISPONIBLE);

        return bicicletas.stream().map(this::convertirADTO).collect(Collectors.toList());
    }

    public List<BicicletaDTO> listarTodas() {
        return bicicletaRepository.findAll().stream()
            .map(this::convertirADTO)
            .collect(Collectors.toList());
    }

    public void cambiarEstado(Bicicleta bicicleta, EstadoBicicleta nuevoEstado) {
        bicicleta.setEstado(nuevoEstado);
        bicicletaRepository.save(bicicleta);
    }

    public BicicletaDTO convertirADTO(Bicicleta bicicleta) {
        return new BicicletaDTO(
            bicicleta.getId(),
            bicicleta.getCodigo(),
            bicicleta.getTipo(),
            bicicleta.getEstado(),
            bicicleta.getFechaCreacion()
        );
    }
}
