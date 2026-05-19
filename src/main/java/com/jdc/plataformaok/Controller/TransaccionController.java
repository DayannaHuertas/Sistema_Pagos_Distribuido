package com.jdc.plataformaok.controllers;

import com.jdc.plataformaok.entities.EventoTransaccionEntity;
import com.jdc.plataformaok.entities.SagaEstadoEntity;
import com.jdc.plataformaok.entities.TransaccionEntity;
import com.jdc.plataformaok.services.CompensacionService;
import com.jdc.plataformaok.services.TransaccionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transacciones")
@RequiredArgsConstructor
public class TransaccionController {

    private final TransaccionService transaccionService;
    private final CompensacionService compensacionService;

    // ============================================================
    // POST /api/transacciones/pagar
    // Iniciar un pago (dispara el Saga completo)
    // ============================================================
    @PostMapping("/pagar")
    public ResponseEntity<?> realizarPago(@RequestBody Map<String, Object> body) {
        try {
            Long cuentaOrigenId  = Long.valueOf(body.get("cuentaOrigenId").toString());
            Long cuentaDestinoId = Long.valueOf(body.get("cuentaDestinoId").toString());
            BigDecimal monto     = new BigDecimal(body.get("monto").toString());
            Long regionId        = Long.valueOf(body.get("regionId").toString());
            String descripcion   = body.getOrDefault("descripcion", "").toString();

            TransaccionEntity transaccion = transaccionService.iniciarPago(
                    cuentaOrigenId, cuentaDestinoId, monto, regionId, descripcion);

            return ResponseEntity.status(HttpStatus.CREATED).body(transaccion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ============================================================
    // GET /api/transacciones
    // Listar todas las transacciones
    // ============================================================
    @GetMapping
    public ResponseEntity<List<TransaccionEntity>> listarTodas() {
        return ResponseEntity.ok(transaccionService.listarTodas());
    }

    // ============================================================
    // GET /api/transacciones/{referencia}
    // Buscar transacción por referencia
    // ============================================================
    @GetMapping("/{referencia}")
    public ResponseEntity<?> buscarPorReferencia(@PathVariable String referencia) {
        try {
            TransaccionEntity transaccion = transaccionService.buscarPorReferencia(referencia);
            return ResponseEntity.ok(transaccion);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ============================================================
    // GET /api/transacciones/{id}/pasos
    // Ver los pasos del Saga de una transacción
    // ============================================================
    @GetMapping("/{id}/pasos")
    public ResponseEntity<List<SagaEstadoEntity>> obtenerPasos(@PathVariable Long id) {
        return ResponseEntity.ok(transaccionService.obtenerPasosSaga(id));
    }

    // ============================================================
    // GET /api/transacciones/{id}/eventos
    // Ver los eventos de una transacción
    // ============================================================
    @GetMapping("/{id}/eventos")
    public ResponseEntity<List<EventoTransaccionEntity>> obtenerEventos(@PathVariable Long id) {
        return ResponseEntity.ok(transaccionService.obtenerEventos(id));
    }

    // ============================================================
    // GET /api/transacciones/{id}/flujo
    // Ver el flujo completo: pasos + eventos + compensaciones
    // ============================================================
    @GetMapping("/{id}/flujo")
    public ResponseEntity<?> obtenerFlujoCompleto(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(compensacionService.obtenerFlujoCompleto(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}