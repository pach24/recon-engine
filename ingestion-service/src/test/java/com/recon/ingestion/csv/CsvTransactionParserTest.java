package com.recon.ingestion.csv;

import com.recon.ingestion.model.Transaction;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvTransactionParserTest {

    private final CsvTransactionParser parser = new CsvTransactionParser("CSV_IMPORT");

    private CsvTransactionParser.Result parse(String csv) throws IOException {
        return parser.parse(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void parsesValidRowsAndDefaultsSourceSystem() throws IOException {
        String csv = """
                transactionId,accountId,amount,currency,occurredAt
                TXN-1,ACC-1,100.50,USD,2026-07-06T10:00:00Z
                """;

        CsvTransactionParser.Result result = parse(csv);

        assertThat(result.errors()).isEmpty();
        assertThat(result.transactions()).hasSize(1);
        Transaction tx = result.transactions().get(0);
        assertThat(tx.transactionId()).isEqualTo("TXN-1");
        assertThat(tx.sourceSystem()).isEqualTo("CSV_IMPORT");
        assertThat(tx.amount()).isEqualByComparingTo("100.50");
    }

    @Test
    void honorsExplicitSourceSystemColumnRegardlessOfOrder() throws IOException {
        String csv = """
                amount,currency,sourceSystem,transactionId,accountId,occurredAt
                42.00,EUR,BANKING,TXN-2,ACC-2,2026-07-06T11:00:00Z
                """;

        CsvTransactionParser.Result result = parse(csv);

        assertThat(result.transactions()).hasSize(1);
        assertThat(result.transactions().get(0).sourceSystem()).isEqualTo("BANKING");
    }

    @Test
    void reportsMalformedRowsWithoutFailingTheBatch() throws IOException {
        String csv = """
                transactionId,accountId,amount,currency,occurredAt
                TXN-3,ACC-3,10.00,USD,2026-07-06T10:00:00Z
                TXN-4,ACC-4,-5.00,USD,2026-07-06T10:00:00Z
                TXN-5,ACC-5,notanumber,USD,2026-07-06T10:00:00Z
                TXN-6,ACC-6,20.00,USD,not-a-date
                """;

        CsvTransactionParser.Result result = parse(csv);

        assertThat(result.transactions()).hasSize(1);
        assertThat(result.errors()).hasSize(3);
        assertThat(result.errors()).extracting(CsvTransactionParser.RowError::line)
                .containsExactly(3, 4, 5);
    }

    @Test
    void supportsQuotedFieldsContainingCommas() throws IOException {
        String csv = """
                transactionId,accountId,amount,currency,occurredAt
                "TXN,7",ACC-7,15.00,USD,2026-07-06T10:00:00Z
                """;

        CsvTransactionParser.Result result = parse(csv);

        assertThat(result.transactions()).hasSize(1);
        assertThat(result.transactions().get(0).transactionId()).isEqualTo("TXN,7");
    }

    @Test
    void rejectsFileMissingRequiredColumns() {
        String csv = "transactionId,amount\nTXN-8,10.00\n";

        assertThatThrownBy(() -> parse(csv))
                .isInstanceOf(CsvFormatException.class)
                .hasMessageContaining("missing required columns");
    }

    @Test
    void rejectsEmptyFile() {
        assertThatThrownBy(() -> parse(""))
                .isInstanceOf(CsvFormatException.class)
                .hasMessageContaining("empty");
    }
}
