package com.fernandoschilder.ipaconsolebackend.service;

import com.fernandoschilder.ipaconsolebackend.model.UserEntity;
import com.fernandoschilder.ipaconsolebackend.repository.UserRepository;
import jakarta.persistence.EntityExistsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.UnsupportedEncodingException;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    public UserEntity createUser(UserEntity user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new EntityExistsException("Username " + user.getUsername() + " already exists");
        }
        UserEntity entity = new UserEntity();
        entity.setUsername(user.getUsername());
        entity.setPassword(encoder.encode(user.getPassword()));
        entity.setEnabled(true);

        return userRepository.save(entity);
    }

    public UserEntity getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("UserEntity Not Found with username: " + username));
    }
}
