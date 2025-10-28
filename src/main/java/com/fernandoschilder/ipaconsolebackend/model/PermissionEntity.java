package com.fernandoschilder.ipaconsolebackend.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "permissions")
public class PermissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long id;

    @NonNull
    @Column(name = "type", unique = true, nullable = false, length = 100)
    private String type;   // p.ej.: "administrador", "visualizar", "ejecutar", "editar_parametros"

    // Lado inverso de RoleEntity.permissions
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<RoleEntity> roles;

    // (Opcional) Permisos aplicados a namespaces
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "permission_namespaces",
            joinColumns = @JoinColumn(name = "permission_id"),
            inverseJoinColumns = @JoinColumn(name = "namespace_id")
    )
    private Set<NamespaceEntity> permission_namespaces;
}
