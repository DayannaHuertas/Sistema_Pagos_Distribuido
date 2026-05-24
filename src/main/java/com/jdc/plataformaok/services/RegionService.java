package com.jdc.plataformaok.services;

import com.jdc.plataformaok.entities.RegionEntity;
import com.jdc.plataformaok.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegionService {

    private final RegionRepository regionRepository;

    // ============================================================
    // CREAR REGION
    // ============================================================
    @Transactional
    public RegionEntity crearRegion(String nombre, String codigo) {
        if (regionRepository.existsByCodigo(codigo)) {
            throw new RuntimeException("Ya existe una región con el código: " + codigo);
        }

        RegionEntity region = RegionEntity.builder()
                .nombre(nombre)
                .codigo(codigo)
                .activa(true)
                .build();

        region = regionRepository.save(region);
        log.info("Región creada: id={}, codigo={}", region.getId(), codigo);
        return region;
    }

    // ============================================================
    // OBTENER POR ID
    // ============================================================
    public RegionEntity obtenerPorId(Long id) {
        return regionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Región no encontrada: " + id));
    }

    // ============================================================
    // OBTENER POR CODIGO
    // ============================================================
    public RegionEntity obtenerPorCodigo(String codigo) {
        return regionRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RuntimeException("Región no encontrada con código: " + codigo));
    }

    // ============================================================
    // LISTAR TODAS
    // ============================================================
    public List<RegionEntity> listarTodas() {
        return regionRepository.findAll();
    }

    // ============================================================
    // LISTAR ACTIVAS
    // ============================================================
    public List<RegionEntity> listarActivas() {
        return regionRepository.findByActivaTrue();
    }

    // ============================================================
    // DESACTIVAR REGION
    // ============================================================
    @Transactional
    public RegionEntity desactivarRegion(Long id) {
        RegionEntity region = regionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Región no encontrada: " + id));
        region.setActiva(false);
        log.info("Región desactivada: id={}", id);
        return regionRepository.save(region);
    }
}