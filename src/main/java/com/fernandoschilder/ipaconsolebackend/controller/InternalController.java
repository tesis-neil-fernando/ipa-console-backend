package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.dto.WorkflowParametersDto;
import com.fernandoschilder.ipaconsolebackend.service.ProcessService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

	private final ProcessService processService;

	public InternalController(ProcessService processService) {
		this.processService = processService;
	}

	@GetMapping("/ping")
	@PreAuthorize("hasRole('INTERNAL')")
	public ResponseEntity<String> ping() {
		return ResponseEntity.ok("pong");
	}

	/**
	 * Return parameters for the given n8n workflow id as a JSON object with dynamic keys.
	 * Example response: { "paramName1": "value1", "paramName2": "value2" }
	 */
	@GetMapping("/workflows/{workflowId}/parameters")
	@PreAuthorize("hasRole('INTERNAL')")
	public ResponseEntity<WorkflowParametersDto> getParametersForWorkflow(@PathVariable String workflowId) {
		var map = processService.getParametersByWorkflowId(workflowId);
		return ResponseEntity.ok(new WorkflowParametersDto(map));
	}

}
