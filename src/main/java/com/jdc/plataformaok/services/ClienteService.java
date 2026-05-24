package com.jdc.plataformaok.services;

import com.jdc.plataformaok.entities.ClienteEntity;
import com.jdc.plataformaok.entities.RegionEntity;
import com.jdc.plataformaok.repository.ClienteRepository;
import com.jdc.plataformaok.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final RegionRepository regionRepository;

    // ============================================================
    // CREAR CLIENTE
    // ============================================================
    @Transactional
    public ClienteEntity crearCliente(String nombre, String email, Long regionId) {
        if (clienteRepository.existsByEmail(email)) {
            throw new RuntimeException("Ya existe un cliente con el email: " + email);
        }

        RegionEntity region = regionRepository.findById(regionId)
                .orElseThrow(() -> new RuntimeException("Región no encontrada: " + regionId));

        ClienteEntity cliente = ClienteEntity.builder()
                .nombre(nombre)
                .email(email)
                .region(region)
                .activo(true)
                .build();

        cliente = clienteRepository.save(cliente);
        log.info("Cliente creado: id={}, email={}", cliente.getId(), email);
        return cliente;
    }

    // ============================================================
    // OBTENER POR ID
    // ============================================================
    public ClienteEntity obtenerPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + id));
    }

    // ============================================================
    // OBTENER POR EMAIL
    // ============================================================
    public ClienteEntity obtenerPorEmail(String email) {
        return clienteRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con email: " + email));
    }

    // ============================================================
    // LISTAR TODOS
    // ============================================================
    public List<ClienteEntity> listarTodos() {
        return clienteRepository.findAll();
    }

    // ============================================================
    // LISTAR ACTIVOS
    // ============================================================
    public List<ClienteEntity> listarActivos() {
        return clienteRepository.findByActivoTrue();
    }

    // ============================================================
    // LISTAR POR REGION
    // ============================================================
    public List<ClienteEntity> listarPorRegion(Long regionId) {
        return clienteRepository.findByRegionId(regionId);
    }

    // ============================================================
    // ACTUALIZAR CLIENTE
    // ============================================================
    @Transactional
    public ClienteEntity actualizarCliente(Long id, String nombre, String email) {
        ClienteEntity cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + id));

        if (!cliente.getEmail().equals(email) && clienteRepository.existsByEmail(email)) {
            throw new RuntimeException("Ya existe un cliente con el email: " + email);
        }

        cliente.setNombre(nombre);
        cliente.setEmail(email);
        log.info("Cliente actualizado: id={}", id);
        return clienteRepository.save(cliente);
    }

    // ============================================================
    // DESACTIVAR CLIENTE
    // ============================================================
    @Transactional
    public ClienteEntity desactivarCliente(Long id) {
        ClienteEntity cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + id));
        cliente.setActivo(false);
        log.info("Cliente desactivado: id={}", id);
        return clienteRepository.save(cliente);
    }
}