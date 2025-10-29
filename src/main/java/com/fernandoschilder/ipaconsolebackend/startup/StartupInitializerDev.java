package com.fernandoschilder.ipaconsolebackend.startup;

import com.fernandoschilder.ipaconsolebackend.model.UserEntity;
import com.fernandoschilder.ipaconsolebackend.repository.UserRepository;
import com.fernandoschilder.ipaconsolebackend.service.WorkflowSyncService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class StartupInitializerDev implements CommandLineRunner {

    private final UserRepository userRepository;
    private final WorkflowSyncService workflowSyncService;
    private final PasswordEncoder encoder;

    public StartupInitializerDev(UserRepository userRepository, WorkflowSyncService workflowSyncService, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.workflowSyncService = workflowSyncService;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) throws Exception {

        //first user
        UserEntity user = new UserEntity();
        user.setUsername("fschilder");
        user.setPassword("May2letter#");
        if (!userRepository.existsByUsername(user.getUsername())) {
            user.setPassword(encoder.encode(user.getPassword()));
            user.setEnabled(true);
            userRepository.save(user);
        }

        //first workflow retrieval
        workflowSyncService.pullAndSave();

    }
}
