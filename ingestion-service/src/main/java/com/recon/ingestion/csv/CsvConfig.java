package com.recon.ingestion.csv;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CsvConfig {

    @Bean
    public CsvTransactionParser csvTransactionParser(
            @Value("${app.csv.default-source-system}") String defaultSourceSystem) {
        return new CsvTransactionParser(defaultSourceSystem);
    }
}
