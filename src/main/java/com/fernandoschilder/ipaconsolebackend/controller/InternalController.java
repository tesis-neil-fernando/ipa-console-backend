package com.fernandoschilder.ipaconsolebackend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal controller for endpoints intended to be called by trusted internal
 * systems. These endpoints are protected by a single-header token (configured
 * via the property `fernandoschilder.app.internal-token`) which the
 * {@code InternalTokenFilter} converts into an authentication with
 * ROLE_INTERNAL.
 */
@RestController
@RequestMapping("/internal")
public class InternalController {

	@GetMapping("/ping")
	@PreAuthorize("hasRole('INTERNAL')")
	public ResponseEntity<String> ping() {
		return ResponseEntity.ok("pong");
	}

}
