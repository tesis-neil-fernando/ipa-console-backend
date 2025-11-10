package com.fernandoschilder.ipaconsolebackend.service.rbac.impl;

import com.fernandoschilder.ipaconsolebackend.dto.rbac.*;
import com.fernandoschilder.ipaconsolebackend.model.*;
import com.fernandoschilder.ipaconsolebackend.repository.*;
import com.fernandoschilder.ipaconsolebackend.service.rbac.RbacService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import com.fernandoschilder.ipaconsolebackend.model.PermissionAction;
import java.security.SecureRandom;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.stream.Collectors;

@Service
@Transactional
public class RbacServiceImpl implements RbacService {

    private final NamespaceRepository namespaceRepository;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ProcessRepository processRepository;
    private final PasswordEncoder passwordEncoder;

    public RbacServiceImpl(NamespaceRepository namespaceRepository, PermissionRepository permissionRepository,
                           RoleRepository roleRepository, UserRepository userRepository, ProcessRepository processRepository,
                           PasswordEncoder passwordEncoder) {
        this.namespaceRepository = namespaceRepository;
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.processRepository = processRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Assumption: permission.type must be unique across the system. To create namespace-scoped
    // permissions we store them with the pattern "{namespaceName}:{perm}" (e.g. "myns:view").
    @Override
    public NamespaceRbacDto createNamespace(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("namespace name required");
        if ("ROOT".equalsIgnoreCase(name)) throw new IllegalArgumentException("namespace name 'ROOT' is reserved");
        if (namespaceRepository.existsByName(name)) throw new IllegalArgumentException("namespace already exists");

        NamespaceEntity ns = new NamespaceEntity(name);
        ns = namespaceRepository.save(ns);

        // create standard permissions for the namespace (VIEW, EXEC, EDIT)
        for (PermissionAction a : PermissionAction.values()) {
            if (permissionRepository.existsByNamespaceAndAction(ns, a)) continue;
            PermissionEntity perm = new PermissionEntity(a, ns);
            // link both sides
            ns.getPermissions().add(perm);
            permissionRepository.save(perm);
        }

        return toNamespaceDto(ns);
    }

    @Override
    public void assignProcessToNamespace(Long processId, Long namespaceId) {
        ProcessEntity p = processRepository.findById(processId).orElseThrow(() -> new NoSuchElementException("process not found"));
        NamespaceEntity ns = namespaceRepository.findById(namespaceId).orElseThrow(() -> new NoSuchElementException("namespace not found"));
        p.setNamespace(ns);
        processRepository.save(p);
    }

    @Override
    public void removeProcessFromNamespace(Long processId, Long namespaceId) {
        ProcessEntity p = processRepository.findById(processId).orElseThrow(() -> new NoSuchElementException("process not found"));
        if (p.getNamespace() == null || !Objects.equals(p.getNamespace().getId(), namespaceId)) {
            throw new IllegalStateException("process is not assigned to the provided namespace");
        }
        p.setNamespace(null);
        processRepository.save(p);
    }

    @Override
    public RoleRbacDto createRole(String name) {
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("role name required");
        // reserve ADMIN role name - it is created by bootstrap and should not be created manually
        if ("ADMIN".equalsIgnoreCase(name)) throw new IllegalArgumentException("role name 'ADMIN' is reserved");
        if (roleRepository.existsByName(name)) throw new IllegalArgumentException("role already exists");
        RoleEntity r = new RoleEntity(name);
        r = roleRepository.save(r);
        return toRoleDto(r);
    }

    @Override
    public CreateUserResponse createUser(String username, String name) {
        if (username == null || username.trim().isEmpty()) throw new IllegalArgumentException("username required");
        if (userRepository.existsByUsername(username)) throw new IllegalArgumentException("username already exists");

        // Generate a secure random password and encode it for storage
        String generatedPassword = generatePassword(12);
        String encoded = passwordEncoder.encode(generatedPassword);

        // create user with provided name (may be null) and enabled by default
        UserEntity u = new UserEntity(username, name, encoded, true);
        u = userRepository.save(u);

    CreateUserResponse resp =
        new CreateUserResponse(u.getUsername(), generatedPassword);
    return resp;
    }

    private String generatePassword(int length) {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Override
    public void updateUserEnabled(Long userId, boolean enabled) {
        if (userId == null) throw new IllegalArgumentException("user id required");
        // Prevent disabling the bootstrap administrator account (username 'administrator').
        UserEntity u = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("user not found"));
        if (u.getUsername() != null && "administrator".equalsIgnoreCase(u.getUsername()) && !enabled) {
            throw new IllegalStateException("bootstrap administrator account cannot be disabled");
        }
        u.setEnabled(enabled);
        userRepository.save(u);
    }

    @Override
    public void assignPermissionToRole(Long roleId, Long permissionId) {
        RoleEntity role = roleRepository.findById(roleId).orElseThrow(() -> new NoSuchElementException("role not found"));
        if (role.getName() != null && "ADMIN".equalsIgnoreCase(role.getName())) {
            throw new IllegalStateException("ADMIN role is immutable; permissions may not be modified");
        }
        PermissionEntity perm = permissionRepository.findById(permissionId).orElseThrow(() -> new NoSuchElementException("permission not found"));
        role.addPermission(perm);
        roleRepository.save(role);
    }

    @Override
    public void removePermissionFromRole(Long roleId, Long permissionId) {
        RoleEntity role = roleRepository.findById(roleId).orElseThrow(() -> new NoSuchElementException("role not found"));
        if (role.getName() != null && "ADMIN".equalsIgnoreCase(role.getName())) {
            throw new IllegalStateException("ADMIN role is immutable; permissions may not be modified");
        }
        PermissionEntity perm = permissionRepository.findById(permissionId).orElseThrow(() -> new NoSuchElementException("permission not found"));
        role.removePermission(perm);
        roleRepository.save(role);
    }

    @Override
    public void assignRoleToUser(Long userId, Long roleId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("user not found"));
        RoleEntity role = roleRepository.findById(roleId).orElseThrow(() -> new NoSuchElementException("role not found"));
        user.addRole(role);
        userRepository.save(user);
    }

    @Override
    public void removeRoleFromUser(Long userId, Long roleId) {
        UserEntity user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("user not found"));
        RoleEntity role = roleRepository.findById(roleId).orElseThrow(() -> new NoSuchElementException("role not found"));
        // Prevent removing the ADMIN role from the bootstrap administrator account only
        if (role.getName() != null && "ADMIN".equalsIgnoreCase(role.getName())) {
            if (user.getUsername() != null && "administrator".equalsIgnoreCase(user.getUsername())) {
                throw new IllegalStateException("ADMIN role assignment cannot be removed from the bootstrap administrator user");
            }
            // allow removing ADMIN from other users
        }
        user.removeRole(role);
        userRepository.save(user);
    }

    @Override
    public UserRbacDto getUserById(Long id) {
        UserEntity u = userRepository.findById(id).orElseThrow(() -> new NoSuchElementException("user not found"));
        return toUserDto(u);
    }

    @Override
    public RoleRbacDto getRoleById(Long id) {
        RoleEntity r = roleRepository.findById(id).orElseThrow(() -> new NoSuchElementException("role not found"));
        return toRoleDto(r);
    }

    @Override
    public NamespaceRbacDto getNamespaceById(Long id) {
        NamespaceEntity ns = namespaceRepository.findById(id).orElseThrow(() -> new NoSuchElementException("namespace not found"));
        return toNamespaceDto(ns);
    }

    @Override
    public List<UserRbacDto> listUsers() {
        return userRepository.findAll().stream().map(this::toUserDto).collect(Collectors.toList());
    }

    @Override
    public List<RoleRbacDto> listRoles() {
        // Return all roles, but for the ADMIN role remove permissions from the DTO so its permissions are not exposed.
        return roleRepository.findAll().stream()
                .map(r -> {
                    RoleRbacDto dto = toRoleDto(r);
                    if (r.getName() != null && "ADMIN".equalsIgnoreCase(r.getName())) {
                        dto.setPermissions(Collections.emptyList());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<NamespaceRbacDto> listNamespaces() {
        return namespaceRepository.findAll().stream().map(this::toNamespaceDto).collect(Collectors.toList());
    }

    @Override
    public List<ProcessRbacDto> listProcesses() {
        return processRepository.findAll().stream().map(this::toProcessDto).collect(Collectors.toList());
    }

    @Override
    public void updatePassword(Long userId, String newPassword) {
        if (userId == null) throw new IllegalArgumentException("user id required");
        if (newPassword == null || newPassword.trim().isEmpty()) throw new IllegalArgumentException("password required");
        UserEntity u = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("user not found"));
        u.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(u);
    }

    @Override
    public void updateProcess(Long processId, String name, String description) {
        if (processId == null) throw new IllegalArgumentException("process id required");
        ProcessEntity p = processRepository.findById(processId).orElseThrow(() -> new NoSuchElementException("process not found"));
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("process name required");
        p.setName(name);
        p.setDescription(description);
        processRepository.save(p);
    }

    @Override
    public void updateUserName(Long userId, String name) {
        if (userId == null) throw new IllegalArgumentException("user id required");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("name required");
        UserEntity u = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("user not found"));
        u.setName(name);
        userRepository.save(u);
    }

    @Override
    public void updateNamespaceName(Long namespaceId, String name) {
        if (namespaceId == null) throw new IllegalArgumentException("namespace id required");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("namespace name required");
        if ("ROOT".equalsIgnoreCase(name)) throw new IllegalArgumentException("namespace name 'ROOT' is reserved");
        NamespaceEntity ns = namespaceRepository.findById(namespaceId).orElseThrow(() -> new NoSuchElementException("namespace not found"));
        // if same name, nothing to do
        if (Objects.equals(ns.getName(), name)) return;
        // ensure no other namespace already uses the name
        namespaceRepository.findByName(name).ifPresent(existing -> {
            if (!Objects.equals(existing.getId(), ns.getId())) throw new IllegalArgumentException("namespace already exists");
        });

        // permissions reference the namespace by FK so renaming does not require updates to permission rows

        ns.setName(name);
        namespaceRepository.save(ns);
    }

    @Override
    public void updateRoleName(Long roleId, String name) {
        if (roleId == null) throw new IllegalArgumentException("role id required");
        if (name == null || name.trim().isEmpty()) throw new IllegalArgumentException("role name required");
        RoleEntity r = roleRepository.findById(roleId).orElseThrow(() -> new NoSuchElementException("role not found"));
        // Prevent renaming the ADMIN role or renaming any role to 'ADMIN'
        if (r.getName() != null && "ADMIN".equalsIgnoreCase(r.getName())) {
            throw new IllegalStateException("ADMIN role is immutable and cannot be renamed");
        }
        if ("ADMIN".equalsIgnoreCase(name)) {
            throw new IllegalArgumentException("role name 'ADMIN' is reserved");
        }
        if (Objects.equals(r.getName(), name)) return;
        roleRepository.findByName(name).ifPresent(existing -> {
            if (!Objects.equals(existing.getId(), r.getId())) throw new IllegalArgumentException("role already exists");
        });
        r.setName(name);
        roleRepository.save(r);
    }

    // --- Converters ---
    private UserRbacDto toUserDto(UserEntity u) {
        UserRbacDto dto = new UserRbacDto();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setName(u.getName());
        dto.setEnabled(u.isEnabled());
        if (u.getRoles() != null) {
            dto.setRoles(u.getRoles().stream().map(r -> {
                RoleRefDto rr = new RoleRefDto(); rr.setId(r.getId()); rr.setName(r.getName()); return rr;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private RoleRbacDto toRoleDto(RoleEntity r) {
        RoleRbacDto dto = new RoleRbacDto();
        dto.setId(r.getId()); dto.setName(r.getName());
        if (r.getUsers() != null) {
            dto.setUsers(r.getUsers().stream().map(u -> { UserRefDto ur = new UserRefDto(); ur.setId(u.getId()); ur.setUsername(u.getUsername()); return ur; }).collect(Collectors.toList()));
        }
        if (r.getPermissions() != null) {
            dto.setPermissions(r.getPermissions().stream().map(p -> {
                PermissionRbacDto pr = new PermissionRbacDto(); pr.setId(p.getId());
                pr.setAction(p.getAction().name().toLowerCase());
                if (p.getNamespace() != null) {
                    NamespaceRefDto nr = new NamespaceRefDto(); nr.setId(p.getNamespace().getId()); nr.setName(p.getNamespace().getName());
                    pr.setNamespace(nr);
                } else {
                    pr.setNamespace(null);
                }
                return pr;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private NamespaceRbacDto toNamespaceDto(NamespaceEntity ns) {
        NamespaceRbacDto dto = new NamespaceRbacDto();
        dto.setId(ns.getId()); dto.setName(ns.getName());
        if (ns.getPermissions() != null) {
            dto.setPermissions(ns.getPermissions().stream().map(p -> {
                PermissionRbacDto pr = new PermissionRbacDto(); pr.setId(p.getId()); pr.setAction(p.getAction().name().toLowerCase());
                NamespaceRefDto nr = new NamespaceRefDto(); nr.setId(ns.getId()); nr.setName(ns.getName()); pr.setNamespace(nr);
                return pr;
            }).collect(Collectors.toList()));
        }
        // roles that are linked to this namespace via permissions
        Set<RoleRefDto> roles = new HashSet<>();
        if (ns.getPermissions() != null) {
            for (PermissionEntity p : ns.getPermissions()) {
                if (p.getRoles() == null) continue;
                for (RoleEntity r : p.getRoles()) {
                    RoleRefDto rr = new RoleRefDto(); rr.setId(r.getId()); rr.setName(r.getName()); roles.add(rr);
                }
            }
        }
        dto.setRoles(new ArrayList<>(roles));
        // include processes belonging to this namespace
        if (ns.getProcesses() != null) {
            dto.setProcesses(ns.getProcesses().stream().map(p -> {
                ProcessRbacDto pr = new ProcessRbacDto(); pr.setId(p.getId()); pr.setName(p.getName());
                    pr.setDescription(p.getDescription());
                if (p.getNamespace() != null) { pr.setNamespaceId(p.getNamespace().getId()); pr.setNamespaceName(p.getNamespace().getName()); }
                return pr;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private ProcessRbacDto toProcessDto(ProcessEntity p) {
        ProcessRbacDto dto = new ProcessRbacDto();
        dto.setId(p.getId()); dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        if (p.getNamespace() != null) {
            dto.setNamespaceId(p.getNamespace().getId()); dto.setNamespaceName(p.getNamespace().getName());
        }
        return dto;
    }
}
