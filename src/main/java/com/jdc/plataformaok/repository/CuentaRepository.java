package com.jdc.plataformaok.repository;

import com.jdc.plataformaok.entities.CuentaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CuentaRepository extends JpaRepository<CuentaEntity, Long> {

    List<CuentaEntity> findByClienteId(Long clienteId);

    List<CuentaEntity> findByActivaTrue();

    // Buscar cuentas con saldo mayor a cierto monto
    List<CuentaEntity> findBySaldoGreaterThanEqual(BigDecimal saldo);

    // Verificar si una cuenta tiene saldo suficiente
    @Query("SELECT c FROM CuentaEntity c WHERE c.id = :id AND c.saldo >= :monto AND c.activa = true")
    Optional<CuentaEntity> findCuentaConSaldoSuficiente(@Param("id") Long id, @Param("monto") BigDecimal monto);

    // Buscar cuenta activa de un cliente
    Optional<CuentaEntity> findByClienteIdAndActivaTrue(Long clienteId);
}