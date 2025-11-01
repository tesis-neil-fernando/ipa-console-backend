package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.dto.JwtResponse;
import com.fernandoschilder.ipaconsolebackend.utils.JwtUtils;
import com.fernandoschilder.ipaconsolebackend.dto.LoginRequest;
import com.fernandoschilder.ipaconsolebackend.security.UserDetailsImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/auth")
@Validated
public class JwtAuthController {

    private final AuthenticationManager authenticationManager;

    private final JwtUtils jwtUtils;

    public JwtAuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }



    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        // defensive trim to avoid accidental leading/trailing spaces
        final String username = loginRequest.username() == null ? "" : loginRequest.username().trim();
        final String password = loginRequest.password();

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtUtils.generateJwtToken(authentication);

    return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getUsername()));

    }

    @GetMapping("/valid")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing Authorization header");
        }
        String ah = authHeader.trim();
        if (!ah.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing or invalid Authorization header");
        }
        String token = ah.substring("Bearer ".length()).trim();
        // basic length sanity check (reject absurdly long tokens)
        if (token.length() > 4096) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Authorization token too long");
        }
        if (jwtUtils.validateJwtToken(token)) {
            return ResponseEntity.ok().body(true);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
    }
}