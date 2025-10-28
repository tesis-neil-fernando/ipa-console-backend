package com.fernandoschilder.ipaconsolebackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "namespaces")
public class NamespaceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "namespace_id")
    private Long id;
    @NonNull
    @Column(name = "name")
    private String name;

    @ManyToMany(mappedBy = "permission_namespaces")
    private Set<PermissionEntity> permissions;

    @OneToMany(mappedBy = "namespace")
    private Set<ProcessEntity> processes;
}
