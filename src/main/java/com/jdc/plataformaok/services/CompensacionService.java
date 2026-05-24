package com.jdc.plataformaok.services;

import com.jdc.plataformaok.entities.CompensacionEntity;
import com.jdc.plataformaok.entities.EventoTransaccionEntity;
import com.jdc.plataformaok.entities.SagaEstadoEntity;
import com.jdc.plataformaok.entities.TransaccionEntity;
import com.jdc.plataformaok.repository.CompensacionRepository;
import com.jdc.plataformaok.repository.EventoTransaccionRepository;
import com.jdc.plataformaok.repository.SagaEstadoRepository;
import com.jdc.plataformaok.repository.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompensacionService {

    private final CompensacionRepository compensacionRepository;
    private final SagaEstadoRepository sagaEstadoRepository;
    private final EventoTransaccionRepository eventoRepository;
    private final TransaccionRepository transaccionRepository;

    // ============================================================
    // LISTAR COMPENSACIONES DE UNA TRANSACCION
    // ============================================================
    public List<CompensacionEntity> listarPorTransaccion(Long transaccionId) {
        return compensacionRepository.findByTransaccionId(transaccionId);
    }

    // ============================================================
    // LISTAR COMPENSACIONES PENDIENTES
    // ============================================================
    public List<CompensacionEntity> listarPendientes() {
        return compensacionRepository.findByEstado(CompensacionEntity.EstadoCompensacion.PENDIENTE);
    }

    // ============================================================
    // LISTAR PASOS SAGA DE UNA TRANSACCION
    // ============================================================
    public List<SagaEstadoEntity> listarPasosSaga(Long transaccionId) {
        return sagaEstadoRepository.findByTransaccionId(transaccionId);
    }

    // ============================================================
    // LISTAR EVENTOS DE UNA TRANSACCION
    // ============================================================
    public List<EventoTransaccionEntity> listarEventos(Long transaccionId) {
        return eventoRepository.findByTransaccionIdOrderByCreadoEnAsc(transaccionId);
    }

    // ============================================================
    // FLUJO COMPLETO DE UNA TRANSACCION
    // ============================================================
    public FlujoTransaccion obtenerFlujoCompleto(Long transaccionId) {
        TransaccionEntity transaccion = transaccionRepository.findById(transaccionId)
                .orElseThrow(() -> new RuntimeException("Transacción no encontrada: " + transaccionId));

        List<SagaEstadoEntity> pasos = sagaEstadoRepository.findByTransaccionId(transaccionId);
        List<EventoTransaccionEntity> eventos = eventoRepository.findByTransaccionIdOrderByCreadoEnAsc(transaccionId);
        List<CompensacionEntity> compensaciones = compensacionRepository.findByTransaccionId(transaccionId);

        return new FlujoTransaccion(transaccion, pasos, eventos, compensaciones);
    }

    // ============================================================
    // CLASE INTERNA: Flujo completo de una transacción
    // ============================================================
    public record FlujoTransaccion(
            TransaccionEntity transaccion,
            List<SagaEstadoEntity> pasos,
            List<EventoTransaccionEntity> eventos,
            List<CompensacionEntity> compensaciones
    ) {}
}