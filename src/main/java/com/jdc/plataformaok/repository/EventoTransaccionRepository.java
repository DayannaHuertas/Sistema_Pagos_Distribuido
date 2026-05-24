package com.jdc.plataformaok.repository;

import com.jdc.plataformaok.entities.EventoTransaccionEntity;
import com.jdc.plataformaok.entities.EventoTransaccionEntity.TipoEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventoTransaccionRepository extends JpaRepository<EventoTransaccionEntity, Long> {

    // Todos los eventos de una transacción en orden cronológico
    List<EventoTransaccionEntity> findByTransaccionIdOrderByCreadoEnAsc(Long transaccionId);

    // Buscar por tipo de evento
    List<EventoTransaccionEntity> findByTipoEvento(TipoEvento tipoEvento);

    // Último evento de una transacción
    Optional<EventoTransaccionEntity> findTopByTransaccionIdOrderByCreadoEnDesc(Long transaccionId);

    // Eventos por origen (node-gateway o java-saga)
    List<EventoTransaccionEntity> findByOrigen(String origen);

    // Verificar si ya existe un evento de cierto tipo para una transacción
    boolean existsByTransaccionIdAndTipoEvento(Long transaccionId, TipoEvento tipoEvento);
}