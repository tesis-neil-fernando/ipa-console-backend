package com.fernandoschilder.ipaconsolebackend.controller;

import com.fernandoschilder.ipaconsolebackend.model.WorkflowEntity;
import com.fernandoschilder.ipaconsolebackend.service.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/workflows")
public class WorkflowController {
    @Autowired
    private WorkflowService workflowService;

    @GetMapping("")
    public List<WorkflowEntity> findAll()
    {
        return workflowService.findAll();
    }
}
