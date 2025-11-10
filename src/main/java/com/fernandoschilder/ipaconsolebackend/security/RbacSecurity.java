package com.fernandoschilder.ipaconsolebackend.security;

import com.fernandoschilder.ipaconsolebackend.model.PermissionAction;
import com.fernandoschilder.ipaconsolebackend.model.ProcessEntity;
import com.fernandoschilder.ipaconsolebackend.model.NamespaceEntity;
import com.fernandoschilder.ipaconsolebackend.repository.ProcessRepository;
import com.fernandoschilder.ipaconsolebackend.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("rbacSecurity")
public class RbacSecurity {

    private final UserRepository userRepository;
    private final ProcessRepository processRepository;

    public RbacSecurity(UserRepository userRepository, ProcessRepository processRepository) {
        this.userRepository = userRepository;
        this.processRepository = processRepository;
    }

    @Transactional(readOnly = true)
    public boolean isAdmin(String username) {
        return userRepository.findByUsername(username)
                .map(u -> u.getRoles().stream().anyMatch(r -> "ADMIN".equals(r.getName())))
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean canViewProcess(String username, Long processId) {
        return canPerform(username, processId, PermissionAction.VIEW);
    }

    @Transactional(readOnly = true)
    public boolean canExecuteProcess(String username, Long processId) {
        return canPerform(username, processId, PermissionAction.EXEC);
    }

    @Transactional(readOnly = true)
    public boolean canEditProcess(String username, Long processId) {
        return canPerform(username, processId, PermissionAction.EDIT);
    }

    private boolean canPerform(String username, Long processId, PermissionAction action) {
        if (isAdmin(username)) return true;
        if (processId == null) return false;
        ProcessEntity p = processRepository.findById(processId).orElse(null);
        if (p == null) return false;
        NamespaceEntity ns = p.getNamespace();
        // check namespace-scoped permissions
        if (ns != null) {
            var allowed = userRepository.findNamespaceIdsByUsernameAndAction(username, action);
            if (allowed != null && allowed.contains(ns.getId())) return true;
        }
        // check global permission (namespace = null)
        return userRepository.userHasGlobalPermission(username, action);
    }
}
