package com.fernandoschilder.ipaconsolebackend.jobs;
import com.fernandoschilder.ipaconsolebackend.service.NeightnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NeightnJob {
    @Autowired
    NeightnService neightnService;

    @Scheduled(cron = "${fernandoschilder.app.cron.retrieve-processes}", zone = "America/Lima")
    public void retriveWorklows() {

        System.out.println("Retrieving workflows: "+neightnService.getAllWorkflows().toString());


    }



    @Scheduled(cron = "${fernandoschilder.app.cron.retrieve-logs}", zone = "America/Lima")
    public void retriveLogs() {
        System.out.println("Retrieving logs: "+neightnService.getAllWorkflows().toString());
    }
}