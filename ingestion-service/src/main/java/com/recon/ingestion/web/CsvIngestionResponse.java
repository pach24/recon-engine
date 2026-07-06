package com.recon.ingestion.web;

import com.recon.ingestion.csv.CsvTransactionParser.RowError;

import java.util.List;

/**
 * Summary returned from a CSV upload: how many data rows were read, how many were
 * published to Kafka, and the per-row rejection reasons for the rest.
 */
public record CsvIngestionResponse(int received, int published, List<RowError> errors) {
}
