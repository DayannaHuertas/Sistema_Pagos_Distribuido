package com.jdc.plataformaok.repository;

import com.jdc.plataformaok.entities.CompensacionEntity;
import com.jdc.plataformaok.entities.CompensacionEntity.EstadoCompensacion;
import com.jdc.plataformaok.entities.CompensacionEntity.TipoCompensacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompensacionRepository extends JpaRepository<CompensacionEntity, Long> {

    // Todas las compensaciones de una transacción
    List<CompensacionEntity> findByTransaccionId(Long transaccionId);

    // Compensaciones pendientes de ejecutar
    List<CompensacionEntity> findByEstado(EstadoCompensacion estado);

    // Buscar compensación por transacción y tipo
    Optional<CompensacionEntity> findByTransaccionIdAndTipo(Long transaccionId, TipoCompensacion tipo);

    // Compensaciones pendientes de una transacción específica
    @Query("SELECT c FROM CompensacionEntity c WHERE c.transaccion.id = :transaccionId AND c.estado = 'PENDIENTE'")
    List<CompensacionEntity> findCompensacionesPendientesByTransaccion(@Param("transaccionId") Long transaccionId);

    // Verificar si ya se compensó un paso
    boolean existsByTransaccionIdAndTipoAndEstado(Long transaccionId, TipoCompensacion tipo, EstadoCompensacion estado);
}