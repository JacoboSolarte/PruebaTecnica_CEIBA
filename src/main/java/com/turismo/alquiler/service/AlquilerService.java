package com.turismo.alquiler.service;

import com.turismo.alquiler.dto.AlquilerDTO;
import com.turismo.alquiler.entity.Alquiler;
import com.turismo.alquiler.entity.Bicicleta;
import com.turismo.alquiler.enums.EstadoAlquiler;
import com.turismo.alquiler.enums.EstadoBicicleta;
import com.turismo.alquiler.exception.AlquilerNotFoundException;
import com.turismo.alquiler.exception.AlquilerYaFinalizadoException;
import com.turismo.alquiler.exception.BicicletaNoDisponibleException;
import com.turismo.alquiler.repository.AlquilerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AlquilerService {

    @Autowired
    private AlquilerRepository alquilerRepository;

    @Autowired
    private BicicletaService bicicletaService;

    @Autowired
    private CostoCalculoService costoCalculoService;

    public AlquilerDTO iniciarAlquiler(String codigoBicicleta, String cliente,
                                       LocalDateTime horaInicio, int duracionEstimadaHoras) {
        Bicicleta bicicleta = bicicletaService.obtenerPorCodigo(codigoBicicleta);

        if (bicicleta.getEstado() != EstadoBicicleta.DISPONIBLE) {
            throw new BicicletaNoDisponibleException(
                "La bicicleta " + codigoBicicleta + " no está disponible"
            );
        }

        Alquiler alquiler = new Alquiler();
        alquiler.setBicicleta(bicicleta);
        alquiler.setCliente(cliente);
        alquiler.setHoraInicio(horaInicio);
        alquiler.setHoraFinEstimada(horaInicio.plusHours(duracionEstimadaHoras));
        alquiler.setEstado(EstadoAlquiler.EN_CURSO);

        bicicletaService.cambiarEstado(bicicleta, EstadoBicicleta.ALQUILADA);

        return convertirADTO(alquilerRepository.save(alquiler));
    }

    public AlquilerDTO finalizarAlquiler(Long alquilerId, LocalDateTime horaFinReal) {
        Alquiler alquiler = alquilerRepository.findById(alquilerId)
            .orElseThrow(() -> new AlquilerNotFoundException("Alquiler " + alquilerId + " no existe"));

        if (alquiler.getEstado() == EstadoAlquiler.FINALIZADO) {
            throw new AlquilerYaFinalizadoException("El alquiler " + alquilerId + " ya fue finalizado");
        }

        BigDecimal costoBase = costoCalculoService.calcularCostoBase(
            alquiler.getHoraInicio(), horaFinReal, alquiler.getBicicleta().getTipo());

        BigDecimal multa = costoCalculoService.calcularMulta(
            alquiler.getHoraFinEstimada(), horaFinReal, alquiler.getBicicleta().getTipo());

        alquiler.setHoraFinReal(horaFinReal);
        alquiler.setCostoBase(costoBase);
        alquiler.setMulta(multa);
        alquiler.setCostoTotal(costoCalculoService.calcularCostoTotal(costoBase, multa));
        alquiler.setEstado(EstadoAlquiler.FINALIZADO);

        bicicletaService.cambiarEstado(alquiler.getBicicleta(), EstadoBicicleta.DISPONIBLE);

        return convertirADTO(alquilerRepository.save(alquiler));
    }

    public List<AlquilerDTO> obtenerHistorial(String codigoBicicleta) {
        Bicicleta bicicleta = bicicletaService.obtenerPorCodigo(codigoBicicleta);
        return alquilerRepository.findByBicicletaOrderByFechaCreacionDesc(bicicleta)
            .stream().map(this::convertirADTO).collect(Collectors.toList());
    }

    public AlquilerDTO obtenerPorId(Long id) {
        Alquiler alquiler = alquilerRepository.findById(id)
            .orElseThrow(() -> new AlquilerNotFoundException("Alquiler " + id + " no existe"));
        return convertirADTO(alquiler);
    }

    private AlquilerDTO convertirADTO(Alquiler alquiler) {
        return new AlquilerDTO(
            alquiler.getId(),
            alquiler.getBicicleta().getCodigo(),
            alquiler.getCliente(),
            alquiler.getHoraInicio(),
            alquiler.getHoraFinEstimada(),
            alquiler.getHoraFinReal(),
            alquiler.getDuracionRealHoras(),
            alquiler.getCostoBase(),
            alquiler.getMulta(),
            alquiler.getCostoTotal(),
            alquiler.getEstado(),
            alquiler.getFechaCreacion()
        );
    }
}
