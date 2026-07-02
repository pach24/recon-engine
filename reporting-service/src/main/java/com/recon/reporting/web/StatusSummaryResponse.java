package com.recon.reporting.web;

import java.util.Map;

public record StatusSummaryResponse(Map<String, Long> countsByStatus) {
}
