package com.jdc.plataformaok.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "compensaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"transaccion", "sagaEstado"})
@EqualsAndHashCode(exclude = {"transaccion", "sagaEstado"})
public class CompensacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 60)
    private TipoCompensacion tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoCompensacion estado = EstadoCompensacion.PENDIENTE;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "ejecutado_en")
    private LocalDateTime ejecutadoEn;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaccion_id", nullable = false)
    private TransaccionEntity transaccion;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saga_estado_id", nullable = false)
    private SagaEstadoEntity sagaEstado;

    @PrePersist
    public void prePersist() {
        this.creadoEn = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        if (this.estado == EstadoCompensacion.EJECUTADA) {
            this.ejecutadoEn = LocalDateTime.now();
        }
    }

    public enum TipoCompensacion {
        REVERTIR_DEBITO, REVERTIR_CREDITO
    }

    public enum EstadoCompensacion {
        PENDIENTE, EJECUTADA, FALLIDA
    }
}