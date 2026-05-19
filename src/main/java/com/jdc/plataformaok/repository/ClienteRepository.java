package com.jdc.plataformaok.repository;

import com.jdc.plataformaok.entities.ClienteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<ClienteEntity, Long> {

    Optional<ClienteEntity> findByEmail(String email);

    List<ClienteEntity> findByActivoTrue();

    List<ClienteEntity> findByRegionId(Long regionId);

    boolean existsByEmail(String email);
}