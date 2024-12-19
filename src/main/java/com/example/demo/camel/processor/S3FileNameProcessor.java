package com.example.demo.camel.processor;

import java.time.LocalDate;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.aws2.s3.AWS2S3Constants;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class S3FileNameProcessor implements Processor {

    private final Boolean primaryDate;

    @Override
    public void process(Exchange exchange) {

        final LocalDate localDate = LocalDate.now();
        final int month = localDate.getMonthValue();
        final int day = localDate.getDayOfMonth();

        final String fileName = "data_%s_%s.csv";
        if (primaryDate) {
            exchange.getMessage()
                .setHeader(AWS2S3Constants.KEY, String.format(fileName, month, day));
        } else {
            exchange.getMessage()
                .setHeader(AWS2S3Constants.KEY, String.format(fileName, month, day - 1));
        }
    }
}
