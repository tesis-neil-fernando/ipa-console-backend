package com.fernandoschilder.ipaconsolebackend.service;

import com.fernandoschilder.ipaconsolebackend.dto.UserViewDTO;
import com.fernandoschilder.ipaconsolebackend.model.RoleEntity;
import com.fernandoschilder.ipaconsolebackend.model.UserEntity;
import com.fernandoschilder.ipaconsolebackend.repository.RoleRepository;
import com.fernandoschilder.ipaconsolebackend.repository.UserRepository;
import com.fernandoschilder.ipaconsolebackend.repository.spec.UserSpecifications;
import jakarta.persistence.EntityExistsException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.Objects; // <-- añade este import

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;   // ← necesario para setUserRoles
    @Autowired
    private PasswordEncoder encoder;

    /* =================== CRUD BÁSICO =================== */

    public UserEntity createUser(UserEntity user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new EntityExistsException("Username " + user.getUsername() + " already exists");
        }
        UserEntity entity = new UserEntity();
        entity.setUsername(user.getUsername());
        entity.setPassword(encoder.encode(user.getPassword()));
        entity.setEnabled(true);
        return userRepository.save(entity);
    }

    public UserEntity getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "UserEntity Not Found with username: " + username));
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UsernameNotFoundException("User with id " + id + " not found");
        }
        userRepository.deleteById(id);
    }

    /* =================== LISTADO / FILTROS =================== */

    public Page<UserViewDTO> listUsers(String q, Boolean enabled, String role, Pageable pageable) {
        Specification<UserEntity> spec = (root, query, cb) -> cb.conjunction();

        if (q != null && !q.isBlank()) {
            spec = spec.and(UserSpecifications.usernameContains(q));
        }
        if (enabled != null) {
            spec = spec.and(UserSpecifications.enabledEquals(enabled));
        }
        if (role != null && !role.isBlank()) {
            spec = spec.and(UserSpecifications.hasRole(role));
        }

        return userRepository.findAll(spec, pageable).map(this::toViewDTO);
    }

    /* =================== ACCIONES DE NEGOCIO =================== */

    @Transactional
    public UserViewDTO updateEnabled(Long id, boolean enabled) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User with id " + id + " not found"));
        user.setEnabled(enabled);
        return toViewDTO(userRepository.save(user));
    }

    @Transactional
    public UserViewDTO setUserRoles(String username, Set<String> roleNames) {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("UserEntity Not Found with username: " + username));

        List<RoleEntity> found = roleRepository.findByNameIn(roleNames == null ? Set.of() : roleNames);
        Set<RoleEntity> roles = new HashSet<>(found);

        Set<String> foundNames = roles.stream().map(RoleEntity::getName).collect(Collectors.toSet());
        Set<String> missing = Optional.ofNullable(roleNames).orElse(Set.of()).stream()
                .filter(rn -> !foundNames.contains(rn))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Roles not found: " + String.join(", ", missing));
        }

        user.setUser_roles(roles);
        userRepository.save(user);
        return toViewDTO(user);
    }

    public UserViewDTO getUserView(String username) {
        UserEntity e = getUserByUsername(username);
        return toViewDTO(e);
    }

    /* =================== MAPPER (incluye namespaces) =================== */

    private UserViewDTO toViewDTO(UserEntity e) {

        // Roles como strings
        Set<String> roles = (e.getUser_roles() == null) ? Set.of()
                : e.getUser_roles().stream()
                .filter(Objects::nonNull)
                .map(RoleEntity::getName)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // Namespaces efectivos: role -> permissions -> permission_namespaces -> name
        Set<String> namespaces =
                Optional.ofNullable(e.getUser_roles()).orElse(Set.of()).stream()
                        .filter(Objects::nonNull)
                        .flatMap(r -> Optional.ofNullable(r.getPermissions()).orElse(Set.of()).stream())
                        .filter(Objects::nonNull)
                        .flatMap(p -> Optional.ofNullable(p.getPermission_namespaces()).orElse(Set.of()).stream())
                        .filter(Objects::nonNull)
                        .map(ns -> ns.getName())
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(LinkedHashSet::new));

        return UserViewDTO.builder()
                .id(e.getId())
                .username(e.getUsername())
                .enabled(e.isEnabled())
                .roles(roles)
                .namespaces(namespaces)   // <— nuevo campo en el DTO
                .build();
    }
}
