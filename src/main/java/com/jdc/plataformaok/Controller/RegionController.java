package com.jdc.plataformaok.controllers;

import com.jdc.plataformaok.entities.RegionEntity;
import com.jdc.plataformaok.services.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/regiones")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    // ============================================================
    // POST /api/regiones
    // Crear una nueva región
    // ============================================================
    @PostMapping
    public ResponseEntity<?> crearRegion(@RequestBody Map<String, Object> body) {
        try {
            String nombre = body.get("nombre").toString();
            String codigo = body.get("codigo").toString();

            RegionEntity region = regionService.crearRegion(nombre, codigo);
            return ResponseEntity.status(HttpStatus.CREATED).body(region);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ============================================================
    // GET /api/regiones
    // Listar todas las regiones
    // ============================================================
    @GetMapping
    public ResponseEntity<List<RegionEntity>> listarTodas() {
        return ResponseEntity.ok(regionService.listarTodas());
    }

    // ============================================================
    // GET /api/regiones/activas
    // Listar regiones activas
    // ============================================================
    @GetMapping("/activas")
    public ResponseEntity<List<RegionEntity>> listarActivas() {
        return ResponseEntity.ok(regionService.listarActivas());
    }

    // ============================================================
    // GET /api/regiones/{id}
    // Obtener región por ID
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(regionService.obtenerPorId(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ============================================================
    // GET /api/regiones/codigo/{codigo}
    // Obtener región por código
    // ============================================================
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<?> obtenerPorCodigo(@PathVariable String codigo) {
        try {
            return ResponseEntity.ok(regionService.obtenerPorCodigo(codigo));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ============================================================
    // DELETE /api/regiones/{id}
    // Desactivar región
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> desactivarRegion(@PathVariable Long id) {
        try {
            regionService.desactivarRegion(id);
            return ResponseEntity.ok(Map.of("mensaje", "Región desactivada", "id", id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}