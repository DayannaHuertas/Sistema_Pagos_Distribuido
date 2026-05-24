package com.jdc.plataformaok.repository;

import com.jdc.plataformaok.entities.TransaccionEntity;
import com.jdc.plataformaok.entities.TransaccionEntity.EstadoTransaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransaccionRepository extends JpaRepository<TransaccionEntity, Long> {

    Optional<TransaccionEntity> findByReferencia(String referencia);

    List<TransaccionEntity> findByEstado(EstadoTransaccion estado);

    List<TransaccionEntity> findByRegionId(Long regionId);

    // Todas las transacciones de una cuenta origen
    List<TransaccionEntity> findByCuentaOrigenId(Long cuentaOrigenId);

    // Todas las transacciones de una cuenta destino
    List<TransaccionEntity> findByCuentaDestinoId(Long cuentaDestinoId);

    // Transacciones pendientes o procesando (para reintentos)
    @Query("SELECT t FROM TransaccionEntity t WHERE t.estado IN ('PENDIENTE', 'PROCESANDO')")
    List<TransaccionEntity> findTransaccionesPendientes();

    // Buscar por referencia y estado
    @Query("SELECT t FROM TransaccionEntity t WHERE t.referencia = :referencia AND t.estado = :estado")
    Optional<TransaccionEntity> findByReferenciaAndEstado(
            @Param("referencia") String referencia,
            @Param("estado") EstadoTransaccion estado);

    boolean existsByReferencia(String referencia);
}