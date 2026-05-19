package com.jdc.plataformaok.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cuentas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"transaccionesOrigen", "transaccionesDestino"})
@ToString(exclude = {"cliente", "transaccionesOrigen", "transaccionesDestino"})
public class CuentaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @DecimalMin(value = "0.00")
    @Column(name = "saldo", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal saldo = BigDecimal.ZERO;

    @NotBlank
    @Column(name = "moneda", nullable = false, length = 5)
    @Builder.Default
    private String moneda = "COP";

    @Column(name = "activa", nullable = false)
    @Builder.Default
    private Boolean activa = true;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClienteEntity cliente;

    @JsonIgnore
    @OneToMany(mappedBy = "cuentaOrigen", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TransaccionEntity> transaccionesOrigen = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "cuentaDestino", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TransaccionEntity> transaccionesDestino = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.creadoEn = LocalDateTime.now();
    }
}