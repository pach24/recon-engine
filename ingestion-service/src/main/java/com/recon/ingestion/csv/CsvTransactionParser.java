package com.recon.ingestion.csv;

import com.recon.ingestion.model.Transaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses an uploaded CSV of transactions into {@link Transaction} records. The first
 * non-empty line is treated as a header; columns are matched by name so column order
 * does not matter. A missing {@code sourceSystem} column (or blank cell) falls back to
 * the configured default so plain legacy exports without provenance still ingest.
 *
 * Expected columns: transactionId, accountId, amount, currency, occurredAt, and
 * optionally sourceSystem.
 */
public class CsvTransactionParser {

    private static final List<String> REQUIRED_COLUMNS =
            List.of("transactionid", "accountid", "amount", "currency", "occurredat");

    private final String defaultSourceSystem;

    public CsvTransactionParser(String defaultSourceSystem) {
        this.defaultSourceSystem = defaultSourceSystem;
    }

    public Result parse(InputStream inputStream) throws IOException {
        List<Transaction> transactions = new ArrayList<>();
        List<RowError> errors = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String headerLine = nextNonEmptyLine(reader);
            if (headerLine == null) {
                throw new CsvFormatException("CSV file is empty");
            }

            Map<String, Integer> columns = indexColumns(splitLine(headerLine));
            List<String> missing = REQUIRED_COLUMNS.stream()
                    .filter(c -> !columns.containsKey(c))
                    .toList();
            if (!missing.isEmpty()) {
                throw new CsvFormatException("CSV is missing required columns: " + missing);
            }

            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank()) {
                    continue;
                }
                try {
                    transactions.add(toTransaction(splitLine(line), columns));
                } catch (RuntimeException e) {
                    errors.add(new RowError(lineNumber, e.getMessage()));
                }
            }
        }

        return new Result(transactions, errors);
    }

    private Transaction toTransaction(List<String> cells, Map<String, Integer> columns) {
        String transactionId = required(cells, columns, "transactionid");
        String accountId = required(cells, columns, "accountid");
        String currency = required(cells, columns, "currency");
        String amountRaw = required(cells, columns, "amount");
        String occurredAtRaw = required(cells, columns, "occurredat");
        String sourceSystem = optional(cells, columns, "sourcesystem");
        if (sourceSystem == null || sourceSystem.isBlank()) {
            sourceSystem = defaultSourceSystem;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountRaw);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid amount: '" + amountRaw + "'");
        }
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("amount must be positive: '" + amountRaw + "'");
        }

        Instant occurredAt;
        try {
            occurredAt = Instant.parse(occurredAtRaw);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "invalid occurredAt (expected ISO-8601 instant): '" + occurredAtRaw + "'");
        }

        return new Transaction(transactionId, sourceSystem, accountId, amount, currency, occurredAt);
    }

    private String required(List<String> cells, Map<String, Integer> columns, String column) {
        String value = optional(cells, columns, column);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("missing required value for column '" + column + "'");
        }
        return value;
    }

    private String optional(List<String> cells, Map<String, Integer> columns, String column) {
        Integer index = columns.get(column);
        if (index == null || index >= cells.size()) {
            return null;
        }
        return cells.get(index).trim();
    }

    private Map<String, Integer> indexColumns(List<String> header) {
        Map<String, Integer> columns = new HashMap<>();
        for (int i = 0; i < header.size(); i++) {
            columns.put(header.get(i).trim().toLowerCase(), i);
        }
        return columns;
    }

    private String nextNonEmptyLine(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.isBlank()) {
                return line;
            }
        }
        return null;
    }

    /**
     * Minimal RFC-4180-style splitter: handles double-quoted fields (so values may
     * contain commas) and escaped quotes ("").
     */
    private List<String> splitLine(String line) {
        List<String> cells = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                cells.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        cells.add(current.toString());
        return cells;
    }

    public record Result(List<Transaction> transactions, List<RowError> errors) {
    }

    public record RowError(int line, String message) {
    }
}
