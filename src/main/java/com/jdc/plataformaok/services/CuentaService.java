package com.jdc.plataformaok.services;

import com.jdc.plataformaok.entities.ClienteEntity;
import com.jdc.plataformaok.entities.CuentaEntity;
import com.jdc.plataformaok.repository.ClienteRepository;
import com.jdc.plataformaok.repository.CuentaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CuentaService {

    private final CuentaRepository cuentaRepository;
    private final ClienteRepository clienteRepository;

    // ============================================================
    // CREAR CUENTA
    // ============================================================
    @Transactional
    public CuentaEntity crearCuenta(Long clienteId, BigDecimal saldoInicial, String moneda) {
        ClienteEntity cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + clienteId));

        CuentaEntity cuenta = CuentaEntity.builder()
                .cliente(cliente)
                .saldo(saldoInicial != null ? saldoInicial : BigDecimal.ZERO)
                .moneda(moneda != null ? moneda : "COP")
                .activa(true)
                .build();

        cuenta = cuentaRepository.save(cuenta);
        log.info("Cuenta creada: id={}, cliente={}", cuenta.getId(), clienteId);
        return cuenta;
    }

    // ============================================================
    // CONSULTAR SALDO
    // ============================================================
    public BigDecimal consultarSaldo(Long cuentaId) {
        CuentaEntity cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada: " + cuentaId));
        return cuenta.getSaldo();
    }

    // ============================================================
    // LISTAR CUENTAS DE UN CLIENTE
    // ============================================================
    public List<CuentaEntity> listarCuentasPorCliente(Long clienteId) {
        return cuentaRepository.findByClienteId(clienteId);
    }

    // ============================================================
    // OBTENER CUENTA POR ID
    // ============================================================
    public CuentaEntity obtenerPorId(Long id) {
        return cuentaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada: " + id));
    }

    // ============================================================
    // LISTAR TODAS
    // ============================================================
    public List<CuentaEntity> listarTodas() {
        return cuentaRepository.findAll();
    }

    // ============================================================
    // DESACTIVAR CUENTA
    // ============================================================
    @Transactional
    public CuentaEntity desactivarCuenta(Long cuentaId) {
        CuentaEntity cuenta = cuentaRepository.findById(cuentaId)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada: " + cuentaId));
        cuenta.setActiva(false);
        log.info("Cuenta desactivada: id={}", cuentaId);
        return cuentaRepository.save(cuenta);
    }

    // ============================================================
    // VERIFICAR SALDO SUFICIENTE
    // ============================================================
    public boolean tieneSaldoSuficiente(Long cuentaId, BigDecimal monto) {
        return cuentaRepository.findCuentaConSaldoSuficiente(cuentaId, monto).isPresent();
    }
}