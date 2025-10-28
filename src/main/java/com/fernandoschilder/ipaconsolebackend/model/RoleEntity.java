package com.fernandoschilder.ipaconsolebackend.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "roles")
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;

    @NonNull
    @Column(name = "name", unique = true, nullable = false, length = 100)
    private String name; // Ejemplo: "administrador", "inteligencia_comercial", "marketing"

    @NonNull
    @Column(name = "description", length = 255)
    private String description;

    // Relación inversa con UserEntity.roles
    @ManyToMany(mappedBy = "user_roles", fetch = FetchType.LAZY)
    private Set<UserEntity> users;

    // Relación directa con PermissionEntity
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",                         // Nombre de la tabla intermedia existente
            joinColumns = @JoinColumn(name = "role_id"),        // Columna FK hacia RoleEntity
            inverseJoinColumns = @JoinColumn(name = "permission_id") // Columna FK hacia PermissionEntity
    )
    private Set<PermissionEntity> permissions; // Cambié el nombre lógico, pero conserva la misma tabla (role_permissions)
}
