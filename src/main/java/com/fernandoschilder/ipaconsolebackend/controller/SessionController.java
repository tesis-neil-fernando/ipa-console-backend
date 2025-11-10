package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.model.SessionEntity;
import com.fernandoschilder.ipaconsolebackend.security.UserDetailsImpl;
import com.fernandoschilder.ipaconsolebackend.service.SessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    private final SessionService sessionService;

    public record SessionDto(String jti, String ipAddress, String userAgent, String os, Date issuedAt, Date expiresAt, Date lastAccessAt, boolean revoked, Long id) {}

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping
    public ResponseEntity<List<SessionDto>> listMySessions(Authentication authentication) {
        UserDetailsImpl u = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = u.getId();
        List<SessionEntity> sessions = sessionService.listActiveSessionsForUser(userId);
        List<SessionDto> dto = sessions.stream().map(s -> new SessionDto(s.getJti(), s.getIpAddress(), s.getUserAgent(), s.getOs(), s.getIssuedAt(), s.getExpiresAt(), s.getLastAccessAt(), s.isRevoked(), s.getId())).collect(Collectors.toList());
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{jti}")
    public ResponseEntity<?> revokeSession(@PathVariable String jti, @RequestParam(name = "hard", required = false, defaultValue = "false") boolean hard, Authentication authentication) {
        UserDetailsImpl u = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = u.getId();
        // check ownership or admin
        boolean isAdmin = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(a -> a.toUpperCase().contains("ADMIN"));
        List<SessionEntity> list = sessionService.listSessionsForUser(userId);
        boolean owns = list.stream().anyMatch(s -> jti.equals(s.getJti()));
        if (!owns && !isAdmin) {
            return ResponseEntity.status(403).body("Forbidden");
        }
        if (hard) {
            sessionService.hardDeleteSession(jti);
        } else {
            sessionService.revokeSession(jti);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/revoke-others")
    public ResponseEntity<?> revokeOthers(@RequestBody(required = false) java.util.Map<String,String> body, @RequestParam(name = "hard", required = false, defaultValue = "false") boolean hard, Authentication authentication) {
        UserDetailsImpl u = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = u.getId();
        String keepJti = body == null ? null : body.get("keepJti");
        if (hard) {
            sessionService.hardDeleteOtherSessions(userId, keepJti);
        } else {
            sessionService.revokeOtherSessions(userId, keepJti);
        }
        return ResponseEntity.ok().build();
    }

}
