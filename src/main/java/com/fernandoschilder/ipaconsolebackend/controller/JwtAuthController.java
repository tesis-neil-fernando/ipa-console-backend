package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.dto.JwtResponse;
import com.fernandoschilder.ipaconsolebackend.utils.JwtUtils;
import com.fernandoschilder.ipaconsolebackend.dto.LoginRequest;
import com.fernandoschilder.ipaconsolebackend.security.UserDetailsImpl;
import com.fernandoschilder.ipaconsolebackend.service.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;


@RestController
@RequestMapping("/auth")
@Validated
public class JwtAuthController {

    private final AuthenticationManager authenticationManager;

    private final JwtUtils jwtUtils;

    private final SessionService sessionService;

    public JwtAuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, SessionService sessionService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.sessionService = sessionService;
    }



    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {

        // defensive trim to avoid accidental leading/trailing spaces
        final String username = loginRequest.username() == null ? "" : loginRequest.username().trim();
        final String password = loginRequest.password();

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtUtils.generateJwtToken(authentication);

        // create a server-side session record with client info
        String jti = jwtUtils.getJtiFromJwtToken(jwt);
        Date issued = jwtUtils.getIssuedAtFromJwtToken(jwt);
        Date expires = jwtUtils.getExpiresAtFromJwtToken(jwt);
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");
        String os = detectOsFromUserAgent(userAgent);
        if (jti != null) {
            try {
                sessionService.createSession(userDetails.getId(), jti, issued, expires, ip, userAgent, os);
            } catch (Exception e) {
                // don't fail the login if session logging fails; log and continue
                // (logger not present here) but we could add logs later
            }
        }

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

    private String detectOsFromUserAgent(String ua) {
        if (ua == null) return "unknown";
        String s = ua.toLowerCase();
        if (s.contains("windows")) return "Windows";
        if (s.contains("macintosh") || s.contains("mac os")) return "macOS";
        if (s.contains("linux")) return "Linux";
        if (s.contains("android")) return "Android";
        if (s.contains("iphone") || s.contains("ipad") || s.contains("ios")) return "iOS";
        return "unknown";
    }
}