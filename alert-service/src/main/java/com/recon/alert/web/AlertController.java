package com.recon.alert.web;

import com.recon.alert.alerting.AlertStore;
import com.recon.alert.model.Alert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/alerts")
public class AlertController {

    private final AlertStore alertStore;

    public AlertController(AlertStore alertStore) {
        this.alertStore = alertStore;
    }

    @GetMapping
    public Collection<Alert> getAlerts() {
        return alertStore.findAll();
    }
}
