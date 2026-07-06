package com.recon.ingestion.csv;

/**
 * Thrown when the uploaded CSV cannot be interpreted at all (empty file or missing
 * required header columns), as opposed to a single malformed data row.
 */
public class CsvFormatException extends RuntimeException {

    public CsvFormatException(String message) {
        super(message);
    }
}
