package com.turismo.alquiler.entity;

import com.turismo.alquiler.enums.EstadoAlquiler;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "alquiler", indexes = {
    @Index(name = "idx_bicicleta", columnList = "bicicleta_id"),
    @Index(name = "idx_estado_alquiler", columnList = "estado"),
    @Index(name = "idx_cliente", columnList = "cliente")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alquiler {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "bicicleta_id", nullable = false)
    private Bicicleta bicicleta;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "El nombre del cliente es requerido")
    private String cliente;

    @Column(nullable = false)
    @NotNull(message = "La hora de inicio es requerida")
    private LocalDateTime horaInicio;

    @Column(nullable = false)
    @NotNull(message = "La hora fin estimada es requerida")
    private LocalDateTime horaFinEstimada;

    @Column(name = "hora_fin_real")
    private LocalDateTime horaFinReal;

    @Column(precision = 10, scale = 2)
    private BigDecimal costoBase;

    @Column(precision = 10, scale = 2)
    private BigDecimal multa;

    @Column(precision = 10, scale = 2)
    private BigDecimal costoTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAlquiler estado;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    public double getDuracionRealHoras() {
        if (horaFinReal == null) {
            return 0;
        }
        Duration duracion = Duration.between(horaInicio, horaFinReal);
        return duracion.toMinutes() / 60.0;
    }

    public double getDuracionEstimadaHoras() {
        Duration duracion = Duration.between(horaInicio, horaFinEstimada);
        return duracion.toMinutes() / 60.0;
    }
}
