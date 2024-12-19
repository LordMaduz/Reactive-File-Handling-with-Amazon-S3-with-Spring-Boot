package com.example.demo.camel.router;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.example.demo.camel.aggregation.ExchangeRateAggregationStrategy;
import com.example.demo.camel.processor.ExchangeRateDataProcessor;
import com.example.demo.camel.processor.S3DataValidationProcessor;
import com.example.demo.camel.processor.S3FileNameProcessor;
import com.example.demo.camel.repo.ExchangeRatesRepository;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@RequiredArgsConstructor
public class ExchangeDataLoaderRoute extends RouteBuilder {

    private final S3DataValidationProcessor s3DataValidationProcessor;
    private final ExchangeRatesRepository exchangeRatesRepository;

    @Override
    public void configure() throws Exception {

        onException(RuntimeException.class)
            .handled(true)
            .log("An unexpected error occurred: ${exception.message}");

        from("quartz://scheduler?cron=0/10+*+*+*+*+?")
            .log("Scheduler triggered. Starting S3 file processing...")
            .process(new S3FileNameProcessor(true))
            .to("aws2-s3://{BUCKET_NAME}?operation=getObject")
            .onException(S3Exception.class)
            .retryAttemptedLogLevel(org.apache.camel.LoggingLevel.WARN)
            .maximumRedeliveries(3)
            .redeliveryDelay(1000)
            .handled(true)
            .choice()
            .when(exchange ->  {
                S3Exception s3Exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, S3Exception.class);
                return s3Exception instanceof NoSuchKeyException;
            })
            .process(new S3FileNameProcessor(false))
            .otherwise()
            .process(new S3FileNameProcessor(true))
            .end()
            .to("aws2-s3://{BUCKET_NAME}?operation=getObject")
            .log("File downloaded from S3: ${header.CamelAwsS3Key}")
            .process(exchange -> s3DataValidationProcessor.setSkipLines(true))
            .split(body().tokenize(StringUtils.LF), new ExchangeRateAggregationStrategy())
            .streaming() // Ensure memory efficiency for large files
            .process(s3DataValidationProcessor) // Process each line
            .log("Aggregated DTOs: ${body}")
            .end()
            .process(new ExchangeRateDataProcessor(exchangeRatesRepository))
            .log("All data processed");
    }
}
