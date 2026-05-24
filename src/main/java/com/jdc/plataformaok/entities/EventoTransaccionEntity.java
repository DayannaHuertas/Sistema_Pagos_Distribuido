package com.jdc.plataformaok.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "eventos_transaccion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"transaccion"})
@EqualsAndHashCode(exclude = {"transaccion"})
public class EventoTransaccionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 80)
    private TipoEvento tipoEvento;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "origen", length = 50)
    private String origen;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaccion_id", nullable = false)
    private TransaccionEntity transaccion;

    @PrePersist
    public void prePersist() {
        this.creadoEn = LocalDateTime.now();
    }

    public enum TipoEvento {
        PAGO_INICIADO, SALDO_VALIDADO, SALDO_INSUFICIENTE,
        DEBITO_REALIZADO, CREDITO_REALIZADO, PAGO_COMPLETADO,
        PAGO_FALLIDO, COMPENSACION_INICIADA, COMPENSACION_COMPLETADA
    }
}