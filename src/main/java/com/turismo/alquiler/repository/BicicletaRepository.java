package com.turismo.alquiler.repository;

import com.turismo.alquiler.entity.Bicicleta;
import com.turismo.alquiler.enums.EstadoBicicleta;
import com.turismo.alquiler.enums.TipoBicicleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BicicletaRepository extends JpaRepository<Bicicleta, Long> {
    Optional<Bicicleta> findByCodigo(String codigo);
    List<Bicicleta> findByEstado(EstadoBicicleta estado);
    List<Bicicleta> findByEstadoAndTipo(EstadoBicicleta estado, TipoBicicleta tipo);
}
