package com.turismo.alquiler.entity;

import com.turismo.alquiler.enums.EstadoBicicleta;
import com.turismo.alquiler.enums.TipoBicicleta;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bicicleta", indexes = {
    @Index(name = "idx_codigo", columnList = "codigo", unique = true),
    @Index(name = "idx_estado", columnList = "estado")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bicicleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoBicicleta tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoBicicleta estado;

    @CreationTimestamp
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @OneToMany(mappedBy = "bicicleta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Alquiler> alquileres = new ArrayList<>();
}
