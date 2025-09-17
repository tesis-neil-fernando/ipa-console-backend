package com.fernandoschilder.ipaconsolebackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;
    @NonNull
    @Column(name = "name")
    private String name;
    @NonNull
    @Column(name = "description")
    private String description;

    @ManyToMany(mappedBy = "user_roles")
    private Set<User> users;

    @ManyToMany
    @JoinTable(name = "role_permissions",
    joinColumns = @JoinColumn(name = "role_id"),
    inverseJoinColumns = @JoinColumn(name = "permission_id"))
    private Set<Permission> role_permissions;

}
