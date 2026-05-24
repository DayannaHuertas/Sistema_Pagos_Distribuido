package com.jdc.plataformaok.repository;

import com.jdc.plataformaok.entities.RegionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegionRepository extends JpaRepository<RegionEntity, Long> {

    Optional<RegionEntity> findByCodigo(String codigo);

    List<RegionEntity> findByActivaTrue();

    boolean existsByCodigo(String codigo);
}