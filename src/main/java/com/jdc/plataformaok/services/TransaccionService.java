package com.jdc.plataformaok.services;

import com.jdc.plataformaok.entities.*;
import com.jdc.plataformaok.entities.SagaEstadoEntity.EstadoPaso;
import com.jdc.plataformaok.entities.SagaEstadoEntity.PasoSaga;
import com.jdc.plataformaok.entities.TransaccionEntity.EstadoTransaccion;
import com.jdc.plataformaok.entities.EventoTransaccionEntity.TipoEvento;
import com.jdc.plataformaok.entities.CompensacionEntity.TipoCompensacion;
import com.jdc.plataformaok.entities.CompensacionEntity.EstadoCompensacion;
import com.jdc.plataformaok.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransaccionService {

    private final TransaccionRepository transaccionRepository;
    private final CuentaRepository cuentaRepository;
    private final SagaEstadoRepository sagaEstadoRepository;
    private final EventoTransaccionRepository eventoRepository;
    private final CompensacionRepository compensacionRepository;

    @Transactional
    public TransaccionEntity iniciarPago(Long cuentaOrigenId, Long cuentaDestinoId,
                                         BigDecimal monto, Long regionId, String descripcion) {
        CuentaEntity cuentaOrigen = cuentaRepository.findById(cuentaOrigenId)
                .orElseThrow(() -> new RuntimeException("Cuenta origen no encontrada: " + cuentaOrigenId));
        CuentaEntity cuentaDestino = cuentaRepository.findById(cuentaDestinoId)
                .orElseThrow(() -> new RuntimeException("Cuenta destino no encontrada: " + cuentaDestinoId));
        RegionEntity region = cuentaOrigen.getCliente().getRegion();

        TransaccionEntity transaccion = TransaccionEntity.builder()
                .referencia(UUID.randomUUID().toString())
                .cuentaOrigen(cuentaOrigen)
                .cuentaDestino(cuentaDestino)
                .monto(monto)
                .region(region)
                .descripcion(descripcion)
                .estado(EstadoTransaccion.PENDIENTE)
                .build();

        transaccion = transaccionRepository.save(transaccion);
        registrarEvento(transaccion, TipoEvento.PAGO_INICIADO, "java-saga",
                "{\"monto\":" + monto + "}");
        ejecutarSaga(transaccion);
        return transaccionRepository.findById(transaccion.getId()).get();
    }

    @Transactional
    public void ejecutarSaga(TransaccionEntity transaccion) {
        transaccion.setEstado(EstadoTransaccion.PROCESANDO);
        transaccionRepository.save(transaccion);

        // PASO 1: Validar saldo — si falla NO se debita nada
        SagaEstadoEntity pasoValidar = crearPaso(transaccion, PasoSaga.VALIDAR_SALDO);
        try {
            validarSaldo(transaccion, pasoValidar);
            pasoValidar.setEstado(EstadoPaso.COMPLETADO);
            sagaEstadoRepository.save(pasoValidar);
        } catch (Exception e) {
            pasoValidar.setEstado(EstadoPaso.FALLIDO);
            pasoValidar.setMensajeError(e.getMessage());
            sagaEstadoRepository.save(pasoValidar);
            transaccion.setEstado(EstadoTransaccion.FALLIDA);
            transaccionRepository.save(transaccion);
            registrarEvento(transaccion, TipoEvento.PAGO_FALLIDO, "java-saga",
                    "{\"error\":\"" + e.getMessage() + "\"}");
            log.error("Pago fallido en validacion: {}", e.getMessage());
            return;
        }

        // PASO 2: Debitar origen
        SagaEstadoEntity pasoDebitar = crearPaso(transaccion, PasoSaga.DEBITAR_ORIGEN);
        try {
            debitarOrigen(transaccion, pasoDebitar);
            pasoDebitar.setEstado(EstadoPaso.COMPLETADO);
            sagaEstadoRepository.save(pasoDebitar);
        } catch (Exception e) {
            pasoDebitar.setEstado(EstadoPaso.FALLIDO);
            pasoDebitar.setMensajeError(e.getMessage());
            sagaEstadoRepository.save(pasoDebitar);
            transaccion.setEstado(EstadoTransaccion.FALLIDA);
            transaccionRepository.save(transaccion);
            registrarEvento(transaccion, TipoEvento.PAGO_FALLIDO, "java-saga",
                    "{\"error\":\"" + e.getMessage() + "\"}");
            return;
        }

        // PASO 3: Acreditar destino
        SagaEstadoEntity pasoAcreditar = crearPaso(transaccion, PasoSaga.ACREDITAR_DESTINO);
        try {
            acreditarDestino(transaccion, pasoAcreditar);
            pasoAcreditar.setEstado(EstadoPaso.COMPLETADO);
            sagaEstadoRepository.save(pasoAcreditar);
        } catch (Exception e) {
            pasoAcreditar.setEstado(EstadoPaso.FALLIDO);
            pasoAcreditar.setMensajeError(e.getMessage());
            sagaEstadoRepository.save(pasoAcreditar);
            compensarDebito(transaccion, pasoDebitar);
            transaccion.setEstado(EstadoTransaccion.COMPENSADA);
            transaccionRepository.save(transaccion);
            registrarEvento(transaccion, TipoEvento.COMPENSACION_COMPLETADA, "java-saga", null);
            return;
        }

        // PASO 4: Notificar
        SagaEstadoEntity pasoNotificar = crearPaso(transaccion, PasoSaga.NOTIFICAR);
        notificar(transaccion, pasoNotificar);
        pasoNotificar.setEstado(EstadoPaso.COMPLETADO);
        sagaEstadoRepository.save(pasoNotificar);

        transaccion.setEstado(EstadoTransaccion.COMPLETADA);
        transaccionRepository.save(transaccion);
        registrarEvento(transaccion, TipoEvento.PAGO_COMPLETADO, "java-saga", null);
        log.info("Saga completada: {}", transaccion.getReferencia());
    }

    private void validarSaldo(TransaccionEntity transaccion, SagaEstadoEntity sagaEstado) {
        CuentaEntity cuenta = transaccion.getCuentaOrigen();
        BigDecimal monto = transaccion.getMonto();

        if (!cuenta.getActiva())
            throw new RuntimeException("La cuenta origen esta inactiva");

        if (cuenta.getSaldo().compareTo(monto) < 0) {
            registrarEvento(transaccion, TipoEvento.SALDO_INSUFICIENTE, "java-saga",
                    "{\"saldo\":" + cuenta.getSaldo() + ",\"monto\":" + monto + "}");
            throw new RuntimeException("Saldo insuficiente. Saldo: " + cuenta.getSaldo() + ", Monto: " + monto);
        }

        sagaEstado.setDatosCompensacion("{\"saldo\":" + cuenta.getSaldo() + "}");
        sagaEstadoRepository.save(sagaEstado);
        registrarEvento(transaccion, TipoEvento.SALDO_VALIDADO, "java-saga",
                "{\"saldo\":" + cuenta.getSaldo() + "}");
    }

    private void debitarOrigen(TransaccionEntity transaccion, SagaEstadoEntity sagaEstado) {
        CuentaEntity cuenta = transaccion.getCuentaOrigen();
        BigDecimal saldoAnterior = cuenta.getSaldo();
        cuenta.setSaldo(saldoAnterior.subtract(transaccion.getMonto()));
        cuentaRepository.save(cuenta);
        sagaEstado.setDatosCompensacion("{\"cuentaId\":" + cuenta.getId() + ",\"monto\":" + transaccion.getMonto() + "}");
        sagaEstadoRepository.save(sagaEstado);
        registrarEvento(transaccion, TipoEvento.DEBITO_REALIZADO, "java-saga",
                "{\"cuentaId\":" + cuenta.getId() + ",\"monto\":" + transaccion.getMonto() + "}");
    }

    private void acreditarDestino(TransaccionEntity transaccion, SagaEstadoEntity sagaEstado) {
        CuentaEntity cuenta = transaccion.getCuentaDestino();
        cuenta.setSaldo(cuenta.getSaldo().add(transaccion.getMonto()));
        cuentaRepository.save(cuenta);
        sagaEstado.setDatosCompensacion("{\"cuentaId\":" + cuenta.getId() + ",\"monto\":" + transaccion.getMonto() + "}");
        sagaEstadoRepository.save(sagaEstado);
        registrarEvento(transaccion, TipoEvento.CREDITO_REALIZADO, "java-saga",
                "{\"cuentaId\":" + cuenta.getId() + ",\"monto\":" + transaccion.getMonto() + "}");
    }

    private void notificar(TransaccionEntity transaccion, SagaEstadoEntity sagaEstado) {
        sagaEstado.setDatosCompensacion("{\"tipo\":\"NOTIFICACION\"}");
        sagaEstadoRepository.save(sagaEstado);
    }

    private void compensarDebito(TransaccionEntity transaccion, SagaEstadoEntity pasoDebitar) {
        CuentaEntity cuenta = transaccion.getCuentaOrigen();
        cuenta.setSaldo(cuenta.getSaldo().add(transaccion.getMonto()));
        cuentaRepository.save(cuenta);
        pasoDebitar.setEstado(EstadoPaso.COMPENSADO);
        sagaEstadoRepository.save(pasoDebitar);
        CompensacionEntity compensacion = CompensacionEntity.builder()
                .transaccion(transaccion)
                .sagaEstado(pasoDebitar)
                .tipo(TipoCompensacion.REVERTIR_DEBITO)
                .estado(EstadoCompensacion.EJECUTADA)
                .motivo("Revertido debito por fallo en acreditacion")
                .build();
        compensacionRepository.save(compensacion);
        registrarEvento(transaccion, TipoEvento.COMPENSACION_INICIADA, "java-saga", null);
        log.info("Debito revertido: cuenta={}", cuenta.getId());
    }

    private SagaEstadoEntity crearPaso(TransaccionEntity transaccion, PasoSaga paso) {
        return sagaEstadoRepository.save(SagaEstadoEntity.builder()
                .transaccion(transaccion)
                .paso(paso)
                .estado(EstadoPaso.EJECUTANDO)
                .build());
    }

    private void registrarEvento(TransaccionEntity transaccion, TipoEvento tipo, String origen, String payload) {
        eventoRepository.save(EventoTransaccionEntity.builder()
                .transaccion(transaccion)
                .tipoEvento(tipo)
                .origen(origen)
                .payload(payload)
                .build());
    }

    public TransaccionEntity buscarPorReferencia(String referencia) {
        return transaccionRepository.findByReferencia(referencia)
                .orElseThrow(() -> new RuntimeException("Transaccion no encontrada: " + referencia));
    }

    public List<TransaccionEntity> listarTodas() {
        return transaccionRepository.findAll();
    }

    public List<SagaEstadoEntity> obtenerPasosSaga(Long transaccionId) {
        return sagaEstadoRepository.findByTransaccionId(transaccionId);
    }

    public List<EventoTransaccionEntity> obtenerEventos(Long transaccionId) {
        return eventoRepository.findByTransaccionIdOrderByCreadoEnAsc(transaccionId);
    }
}