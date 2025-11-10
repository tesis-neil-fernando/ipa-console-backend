package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.model.SessionEntity;
import com.fernandoschilder.ipaconsolebackend.security.UserDetailsImpl;
import com.fernandoschilder.ipaconsolebackend.service.SessionService;
import com.fernandoschilder.ipaconsolebackend.repository.UserRepository;
import com.fernandoschilder.ipaconsolebackend.repository.NamespaceRepository;
import com.fernandoschilder.ipaconsolebackend.model.PermissionAction;
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

    private final UserRepository userRepository;
    private final NamespaceRepository namespaceRepository;

    public SessionController(SessionService sessionService, UserRepository userRepository, NamespaceRepository namespaceRepository) {
        this.sessionService = sessionService;
        this.userRepository = userRepository;
        this.namespaceRepository = namespaceRepository;
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

    /**
     * Return the namespace scope for the current user: list of namespaces with the allowed actions
     * and global permission flags for each action.
     */
    @GetMapping("/namespace-scope")
    public ResponseEntity<?> getNamespaceScope(Authentication authentication) {
        UserDetailsImpl u = (UserDetailsImpl) authentication.getPrincipal();
        String username = u.getUsername();

        // For each action, fetch namespace ids
        java.util.Map<PermissionAction, java.util.List<Long>> idsByAction = new java.util.EnumMap<>(PermissionAction.class);
        for (PermissionAction a : PermissionAction.values()) {
            java.util.List<Long> ids = userRepository.findNamespaceIdsByUsernameAndAction(username, a);
            idsByAction.put(a, ids == null ? java.util.List.of() : ids);
        }

        // Build a map of namespaceId -> set of actions
        java.util.Map<Long, java.util.Set<String>> nsActions = new java.util.HashMap<>();
        for (var entry : idsByAction.entrySet()) {
            PermissionAction action = entry.getKey();
            for (Long nsId : entry.getValue()) {
                nsActions.computeIfAbsent(nsId, k -> new java.util.HashSet<>()).add(action.name());
            }
        }

        // Build DTOs with namespace name
        java.util.List<java.util.Map<String,Object>> namespaces = new java.util.ArrayList<>();
        for (var e : nsActions.entrySet()) {
            Long nsId = e.getKey();
            String name = namespaceRepository.findById(nsId).map(n -> n.getName()).orElse(String.valueOf(nsId));
            java.util.List<String> acts = new java.util.ArrayList<>(e.getValue());
            java.util.Map<String,Object> dto = new java.util.HashMap<>();
            dto.put("id", nsId);
            dto.put("name", name);
            dto.put("actions", acts);
            namespaces.add(dto);
        }

        // Also include global flags
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("namespaces", namespaces);
        for (PermissionAction a : PermissionAction.values()) {
            boolean global = userRepository.userHasGlobalPermission(username, a);
            result.put("global_" + a.name().toLowerCase(), global);
        }

        return ResponseEntity.ok(result);
    }

}
