package com.jdc.plataformaok.repository;

import com.jdc.plataformaok.entities.SagaEstadoEntity;
import com.jdc.plataformaok.entities.SagaEstadoEntity.EstadoPaso;
import com.jdc.plataformaok.entities.SagaEstadoEntity.PasoSaga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SagaEstadoRepository extends JpaRepository<SagaEstadoEntity, Long> {

    // Todos los pasos de una transacción
    List<SagaEstadoEntity> findByTransaccionId(Long transaccionId);

    // Un paso específico de una transacción
    Optional<SagaEstadoEntity> findByTransaccionIdAndPaso(Long transaccionId, PasoSaga paso);

    // Pasos fallidos de una transacción (para compensar)
    List<SagaEstadoEntity> findByTransaccionIdAndEstado(Long transaccionId, EstadoPaso estado);

    // Pasos que necesitan compensación
    @Query("SELECT s FROM SagaEstadoEntity s WHERE s.transaccion.id = :transaccionId AND s.estado = 'COMPLETADO' ORDER BY s.id DESC")
    List<SagaEstadoEntity> findPasosCompletadosByTransaccion(@Param("transaccionId") Long transaccionId);

    // Verificar si todos los pasos están completados
    @Query("SELECT COUNT(s) FROM SagaEstadoEntity s WHERE s.transaccion.id = :transaccionId AND s.estado != 'COMPLETADO'")
    long countPasosIncompletos(@Param("transaccionId") Long transaccionId);
}