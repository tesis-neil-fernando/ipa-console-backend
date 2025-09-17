package com.fernandoschilder.ipaconsolebackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "permissions")
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long id;
    @NonNull
    @Column(name = "type")
    private String type;

    @ManyToMany(mappedBy = "role_permissions")
    private Set<Role> roles;

    @ManyToMany
    @JoinTable(name="permission_namespaces",
    joinColumns = @JoinColumn(name = "namespaces_id"),
    inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Namespace> permission_namespaces;
}
