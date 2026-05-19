package com.jdc.plataformaok.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "saga_estados")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"compensaciones"})
@ToString(exclude = {"transaccion", "compensaciones"})
public class SagaEstadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "paso", nullable = false, length = 60)
    private PasoSaga paso;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoPaso estado = EstadoPaso.PENDIENTE;

    @Column(name = "intento", nullable = false)
    @Builder.Default
    private Integer intento = 1;

    @Column(name = "mensaje_error", columnDefinition = "TEXT")
    private String mensajeError;

    @Column(name = "datos_compensacion", columnDefinition = "TEXT")
    private String datosCompensacion;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaccion_id", nullable = false)
    private TransaccionEntity transaccion;

    @JsonIgnore
    @OneToMany(mappedBy = "sagaEstado", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CompensacionEntity> compensaciones = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.creadoEn = LocalDateTime.now();
        this.actualizadoEn = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }

    public enum PasoSaga {
        VALIDAR_SALDO, DEBITAR_ORIGEN, ACREDITAR_DESTINO, NOTIFICAR
    }

    public enum EstadoPaso {
        PENDIENTE, EJECUTANDO, COMPLETADO, FALLIDO, COMPENSANDO, COMPENSADO
    }
}