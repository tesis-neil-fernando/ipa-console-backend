package com.fernandoschilder.ipaconsolebackend.startup;

import com.fernandoschilder.ipaconsolebackend.model.User;
import com.fernandoschilder.ipaconsolebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class StartupInitializerDev implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Override
    public void run(String... args) throws Exception {
        User user = new User();
        user.setUsername("fschilder");
        user.setPassword("Password123");
        if (!userRepository.existsByUsername(user.getUsername())) {
            user.setUsername(user.getUsername());
            user.setPassword(encoder.encode(user.getPassword()));
            user.setEnabled(true);
            userRepository.save(user);
        }
    }
}
