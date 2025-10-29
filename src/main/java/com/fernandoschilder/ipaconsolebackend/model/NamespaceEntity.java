package com.fernandoschilder.ipaconsolebackend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@EqualsAndHashCode(of = "id")
@Entity
@Table(name = "namespaces")
public class NamespaceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "namespace_id")
    private Long id;

    @NonNull
    @Column(name = "name", unique = true, nullable = false, length = 120)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    // Evita recursion infinita en JSON
    @JsonIgnore
    @ManyToMany(mappedBy = "permission_namespaces", fetch = FetchType.LAZY)
    private Set<PermissionEntity> permissions;

    // Si tus procesos tienen un campo "namespace", esto se mantiene
    @JsonIgnore
    @OneToMany(mappedBy = "namespace", fetch = FetchType.LAZY)
    private Set<ProcessEntity> processes;
}
