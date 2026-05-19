package com.jdc.plataformaok.controllers;

import com.jdc.plataformaok.entities.CuentaEntity;
import com.jdc.plataformaok.services.CuentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cuentas")
@RequiredArgsConstructor
public class CuentaController {

    private final CuentaService cuentaService;

    // ============================================================
    // POST /api/cuentas
    // Crear una nueva cuenta
    // ============================================================
    @PostMapping
    public ResponseEntity<?> crearCuenta(@RequestBody Map<String, Object> body) {
        try {
            Long clienteId       = Long.valueOf(body.get("clienteId").toString());
            BigDecimal saldo     = new BigDecimal(body.getOrDefault("saldoInicial", "0").toString());
            String moneda        = body.getOrDefault("moneda", "COP").toString();

            CuentaEntity cuenta = cuentaService.crearCuenta(clienteId, saldo, moneda);
            return ResponseEntity.status(HttpStatus.CREATED).body(cuenta);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ============================================================
    // GET /api/cuentas
    // Listar todas las cuentas
    // ============================================================
    @GetMapping
    public ResponseEntity<List<CuentaEntity>> listarTodas() {
        return ResponseEntity.ok(cuentaService.listarTodas());
    }

    // ============================================================
    // GET /api/cuentas/{id}
    // Obtener cuenta por ID
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(cuentaService.obtenerPorId(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ============================================================
    // GET /api/cuentas/{id}/saldo
    // Consultar saldo de una cuenta
    // ============================================================
    @GetMapping("/{id}/saldo")
    public ResponseEntity<?> consultarSaldo(@PathVariable Long id) {
        try {
            BigDecimal saldo = cuentaService.consultarSaldo(id);
            return ResponseEntity.ok(Map.of("cuentaId", id, "saldo", saldo));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ============================================================
    // GET /api/cuentas/cliente/{clienteId}
    // Listar cuentas de un cliente
    // ============================================================
    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<CuentaEntity>> listarPorCliente(@PathVariable Long clienteId) {
        return ResponseEntity.ok(cuentaService.listarCuentasPorCliente(clienteId));
    }

    // ============================================================
    // DELETE /api/cuentas/{id}
    // Desactivar cuenta
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> desactivarCuenta(@PathVariable Long id) {
        try {
            CuentaEntity cuenta = cuentaService.desactivarCuenta(id);
            return ResponseEntity.ok(Map.of("mensaje", "Cuenta desactivada", "id", cuenta.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}