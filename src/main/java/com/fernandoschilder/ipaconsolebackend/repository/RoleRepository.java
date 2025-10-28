package com.fernandoschilder.ipaconsolebackend.repository;

import com.fernandoschilder.ipaconsolebackend.model.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    // Búsqueda exacta por nombre de rol (único)
    Optional<RoleEntity> findByName(String name);

    // Verificación de duplicados
    boolean existsByName(String name);

    // (Opcionales útiles)
    List<RoleEntity> findByNameIn(Collection<String> names);          // para cargar varios roles por nombre
    Optional<RoleEntity> findByNameIgnoreCase(String name);           // si quieres ignorar mayúsculas
}
