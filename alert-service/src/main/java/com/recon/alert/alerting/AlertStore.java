package com.recon.alert.alerting;

import com.recon.alert.model.Alert;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AlertStore {

    private final Map<String, Alert> alertsByTransactionId = new ConcurrentHashMap<>();

    public void save(Alert alert) {
        alertsByTransactionId.put(alert.transactionId(), alert);
    }

    public Collection<Alert> findAll() {
        return alertsByTransactionId.values();
    }
}
