package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.dto.UserViewDTO;
import com.fernandoschilder.ipaconsolebackend.dto.UserCreateDto;
import com.fernandoschilder.ipaconsolebackend.model.UserEntity;
import com.fernandoschilder.ipaconsolebackend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Obtener un usuario por username
    @GetMapping("/{username}")
    public UserViewDTO getUser(@PathVariable String username) {
        return userService.getUserView(username);
    }

    // Crear usuario
    @PostMapping("")
    public ResponseEntity<UserViewDTO> createUser(@RequestBody @jakarta.validation.Valid UserCreateDto user) {
        UserViewDTO created = userService.createUser(user);
        return ResponseEntity.created(java.net.URI.create("/users/" + created.username())).body(created);
    }

    // Eliminar usuario por id
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User with id " + id + " deleted successfully.");
    }

    // Listado paginado + filtros (q, enabled, role)
    @GetMapping("")
    public Page<UserViewDTO> listUsers(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "enabled", required = false) Boolean enabled,
            @RequestParam(value = "role", required = false) String role,
            @PageableDefault(size = 20, sort = "username") Pageable pageable
    ) {
        return userService.listUsers(q, enabled, role, pageable);
    }

    // ===== AJUSTES DE RUTA AQUÍ =====
    // Asignar/actualizar roles de un usuario
    @PutMapping("/{username}/roles")
    public UserViewDTO setRoles(@PathVariable String username,
                                @RequestBody Set<String> roles) {
        return userService.setUserRoles(username, roles);
    }

    // Activar/Desactivar usuario
    @PatchMapping("/{id}/enabled")
    public UserViewDTO patchEnabled(@PathVariable Long id,
                                    @RequestParam boolean value) {
        return userService.updateEnabled(id, value);
    }
}
