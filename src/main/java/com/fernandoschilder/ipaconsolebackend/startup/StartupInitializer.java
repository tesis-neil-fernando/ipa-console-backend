package com.fernandoschilder.ipaconsolebackend.startup;

import com.fernandoschilder.ipaconsolebackend.model.UserEntity;
import com.fernandoschilder.ipaconsolebackend.model.RoleEntity;
import com.fernandoschilder.ipaconsolebackend.model.PermissionEntity;
import com.fernandoschilder.ipaconsolebackend.model.PermissionAction;
import com.fernandoschilder.ipaconsolebackend.repository.UserRepository;
import com.fernandoschilder.ipaconsolebackend.repository.RoleRepository;
import com.fernandoschilder.ipaconsolebackend.repository.PermissionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class StartupInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final String initAdminPassword;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public StartupInitializer(UserRepository userRepository, RoleRepository roleRepository, PermissionRepository permissionRepository, PasswordEncoder encoder, @Value("${INIT_ADMIN_PASSWORD}") String initAdminPassword) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.encoder = encoder;
        this.initAdminPassword = initAdminPassword;
    }

    @Override
    public void run(String... args) throws Exception {
        if (initAdminPassword == null || initAdminPassword.trim().isEmpty()) {
            throw new IllegalStateException("INIT_ADMIN_PASSWORD must be provided as an environment variable or property for initial administrator creation.");
        }

        UserEntity user = new UserEntity();
        user.setUsername("administrator");
        if (!userRepository.existsByUsername(user.getUsername())) {
            user.setPassword(encoder.encode(initAdminPassword));
            user.setName("Administrador");
            user.setEnabled(true);
            // create or fetch ADMIN role
            RoleEntity adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> {
                RoleEntity r = new RoleEntity("ADMIN");
                return roleRepository.save(r);
            });

            // ensure global permissions (namespace = null) exist and are assigned to ADMIN
            for (PermissionAction a : PermissionAction.values()) {
                PermissionEntity perm = permissionRepository.findByNamespaceIsNullAndAction(a).orElseGet(() -> {
                    PermissionEntity p = new PermissionEntity(a);
                    return permissionRepository.save(p);
                });
                // link permission <-> role if not already linked
                if (adminRole.getPermissions() == null || !adminRole.getPermissions().contains(perm)) {
                    adminRole.addPermission(perm);
                }
            }
            roleRepository.save(adminRole);

            // assign ADMIN role to the initial user
            user.addRole(adminRole);
            userRepository.save(user);
        }
    }
}
