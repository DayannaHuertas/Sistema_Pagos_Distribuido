package com.jdc.plataformaok.controllers;

import com.jdc.plataformaok.entities.ClienteEntity;
import com.jdc.plataformaok.services.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    // ============================================================
    // POST /api/clientes
    // Crear un nuevo cliente
    // ============================================================
    @PostMapping
    public ResponseEntity<?> crearCliente(@RequestBody Map<String, Object> body) {
        try {
            String nombre  = body.get("nombre").toString();
            String email   = body.get("email").toString();
            Long regionId  = Long.valueOf(body.get("regionId").toString());

            ClienteEntity cliente = clienteService.crearCliente(nombre, email, regionId);
            return ResponseEntity.status(HttpStatus.CREATED).body(cliente);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ============================================================
    // GET /api/clientes
    // Listar todos los clientes
    // ============================================================
    @GetMapping
    public ResponseEntity<List<ClienteEntity>> listarTodos() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    // ============================================================
    // GET /api/clientes/activos
    // Listar clientes activos
    // ============================================================
    @GetMapping("/activos")
    public ResponseEntity<List<ClienteEntity>> listarActivos() {
        return ResponseEntity.ok(clienteService.listarActivos());
    }

    // ============================================================
    // GET /api/clientes/{id}
    // Obtener cliente por ID
    // ============================================================
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(clienteService.obtenerPorId(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ============================================================
    // GET /api/clientes/email/{email}
    // Obtener cliente por email
    // ============================================================
    @GetMapping("/email/{email}")
    public ResponseEntity<?> obtenerPorEmail(@PathVariable String email) {
        try {
            return ResponseEntity.ok(clienteService.obtenerPorEmail(email));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ============================================================
    // GET /api/clientes/region/{regionId}
    // Listar clientes por región
    // ============================================================
    @GetMapping("/region/{regionId}")
    public ResponseEntity<List<ClienteEntity>> listarPorRegion(@PathVariable Long regionId) {
        return ResponseEntity.ok(clienteService.listarPorRegion(regionId));
    }

    // ============================================================
    // PUT /api/clientes/{id}
    // Actualizar cliente
    // ============================================================
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCliente(@PathVariable Long id,
                                               @RequestBody Map<String, Object> body) {
        try {
            String nombre = body.get("nombre").toString();
            String email  = body.get("email").toString();

            ClienteEntity cliente = clienteService.actualizarCliente(id, nombre, email);
            return ResponseEntity.ok(cliente);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ============================================================
    // DELETE /api/clientes/{id}
    // Desactivar cliente
    // ============================================================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> desactivarCliente(@PathVariable Long id) {
        try {
            clienteService.desactivarCliente(id);
            return ResponseEntity.ok(Map.of("mensaje", "Cliente desactivado", "id", id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}