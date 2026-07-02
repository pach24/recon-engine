package com.recon.reporting.web;

import com.recon.reporting.persistence.ReconciliationResultRepository;
import com.recon.reporting.persistence.ReconciliationStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reports")
public class ReportingController {

    private final ReconciliationResultRepository reconciliationResultRepository;

    public ReportingController(ReconciliationResultRepository reconciliationResultRepository) {
        this.reconciliationResultRepository = reconciliationResultRepository;
    }

    @GetMapping("/summary")
    public StatusSummaryResponse summary() {
        var countsByStatus = Arrays.stream(ReconciliationStatus.values())
                .collect(Collectors.toMap(
                        Enum::name,
                        reconciliationResultRepository::countByStatus,
                        (a, b) -> a,
                        java.util.LinkedHashMap::new
                ));
        return new StatusSummaryResponse(countsByStatus);
    }

    @GetMapping("/transactions")
    public List<ReconciliationResultResponse> transactions(
            @RequestParam("status") ReconciliationStatus status) {
        return reconciliationResultRepository.findByStatus(status).stream()
                .map(ReconciliationResultResponse::from)
                .toList();
    }
}
