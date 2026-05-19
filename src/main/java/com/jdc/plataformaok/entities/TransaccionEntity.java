package com.jdc.plataformaok.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transacciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"sagaEstados", "eventos", "compensaciones"})
@ToString(exclude = {"cuentaOrigen", "cuentaDestino", "region", "sagaEstados", "eventos", "compensaciones"})
public class TransaccionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "referencia", nullable = false, unique = true, length = 100)
    private String referencia;

    @NotNull
    @DecimalMin(value = "0.01")
    @Column(name = "monto", nullable = false, precision = 15, scale = 2)
    private BigDecimal monto;

    @NotBlank
    @Column(name = "moneda", nullable = false, length = 5)
    @Builder.Default
    private String moneda = "COP";

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoTransaccion estado = EstadoTransaccion.PENDIENTE;

    @Size(max = 300)
    @Column(name = "descripcion", length = 300)
    private String descripcion;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_origen", nullable = false)
    private CuentaEntity cuentaOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_destino", nullable = false)
    private CuentaEntity cuentaDestino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private RegionEntity region;

    @JsonIgnore
    @OneToMany(mappedBy = "transaccion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<SagaEstadoEntity> sagaEstados = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "transaccion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EventoTransaccionEntity> eventos = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "transaccion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
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

    public enum EstadoTransaccion {
        PENDIENTE, PROCESANDO, COMPLETADA, FALLIDA, COMPENSADA
    }
}