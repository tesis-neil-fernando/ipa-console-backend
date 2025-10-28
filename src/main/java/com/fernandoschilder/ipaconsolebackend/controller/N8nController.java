package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.model.N8nWorkflowEntity;
import com.fernandoschilder.ipaconsolebackend.response.N8nWorkflowsResponse;
import com.fernandoschilder.ipaconsolebackend.service.N8nService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/n8n")
public class N8nController {
    @Autowired
    private N8nService service;



}