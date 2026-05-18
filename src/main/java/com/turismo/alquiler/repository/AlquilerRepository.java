package com.turismo.alquiler.repository;

import com.turismo.alquiler.entity.Alquiler;
import com.turismo.alquiler.entity.Bicicleta;
import com.turismo.alquiler.enums.EstadoAlquiler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlquilerRepository extends JpaRepository<Alquiler, Long> {
    List<Alquiler> findByBicicletaOrderByFechaCreacionDesc(Bicicleta bicicleta);
    List<Alquiler> findByEstado(EstadoAlquiler estado);
    Optional<Alquiler> findByIdAndEstado(Long id, EstadoAlquiler estado);
}
