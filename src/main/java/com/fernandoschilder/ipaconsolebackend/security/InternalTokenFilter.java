package com.fernandoschilder.ipaconsolebackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * A simple filter that authenticates requests to /internal/** when they include a
 * configured header token (X-Internal-Token). If the token matches, an
 * Authentication with ROLE_INTERNAL is set in the SecurityContext.
 */
public class InternalTokenFilter extends OncePerRequestFilter {

    private static final String INTERNAL_HEADER = "X-Internal-Token";
    private final String internalToken;

    public InternalTokenFilter(String internalToken) {
        this.internalToken = internalToken == null ? "" : internalToken.trim();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Only filter internal paths
        String path = request.getRequestURI();
        return !path.startsWith("/internal");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // If no internal token configured, skip
        if (internalToken.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        // If already authenticated, don't override
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String header = request.getHeader(INTERNAL_HEADER);
            if (header != null && header.equals(internalToken)) {
                // create a simple authentication for internal callers
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        "internal",
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_INTERNAL"))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
