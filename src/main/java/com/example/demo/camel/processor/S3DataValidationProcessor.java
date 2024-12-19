package com.example.demo.camel.processor;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.stereotype.Component;

import com.example.demo.camel.model.ExchangeRateRecord;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class S3DataValidationProcessor implements Processor {

    @Setter
    private boolean skipLines = true;

    @Override
    public void process(Exchange exchange) {

        if (skipLines) {
            skipLines = false;
            return;
        }

        final String line = exchange.getIn().getBody(String.class);
        final String delimiter = "\\|";
        final String[] fields = line.split(delimiter);

        // Validation: Ensure the line has the expected number of fields
        if (fields.length < 10 || Arrays.stream(fields).anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Invalid Data: " + line);
        }

        final ExchangeRateRecord record = new ExchangeRateRecord(
            LocalDate.parse(fields[0]),
            fields[1],
            fields[2],
            fields[3],
            Double.valueOf(fields[4]),
            Integer.valueOf(fields[5]),
            fields[6],
            fields[7],
            fields[8],
            LocalDate.parse(fields[9].trim())
        );

        exchange.getIn().setBody(record, ExchangeRateRecord.class);
    }
}
