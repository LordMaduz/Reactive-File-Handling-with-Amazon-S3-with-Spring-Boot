package com.example.demo.camel.router;

import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.example.demo.camel.aggregation.ExchangeRateAggregationStrategy;
import com.example.demo.camel.processor.ExchangeRateDataProcessor;
import com.example.demo.camel.processor.S3DataValidationProcessor;
import com.example.demo.camel.processor.S3FileNameProcessor;
import com.example.demo.camel.repo.ExchangeRatesRepository;
import com.example.demo.camel.service.CamelExchangeEnhancer;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@RequiredArgsConstructor
public class ExchangeDataLoaderRoute extends RouteBuilder {

    private final S3DataValidationProcessor s3DataValidationProcessor;
    private final CamelExchangeEnhancer camelExchangeEnhancer;
    private final ExchangeRateDataProcessor exchangeRateDataProcessor;
    private final ExchangeRatesRepository exchangeRatesRepository;
    private final String METHOD_SKIP_HEADERS = "skipHeaders";


    @Override
    public void configure() throws Exception {

        onException(S3Exception.class)
            .retryAttemptedLogLevel(LoggingLevel.WARN)
            .maximumRedeliveries(3)
            .redeliveryDelay(1000)
            .handled(true)
            .choice()
            .when(exchange -> {
                final S3Exception s3Exception = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, S3Exception.class);
                return s3Exception instanceof NoSuchKeyException;
            })
            .process(new S3FileNameProcessor(false))
            .otherwise()
            .process(new S3FileNameProcessor(true))
            .end()
            .to("direct:S3FileDownload");

        onException(RuntimeException.class)
            .retryAttemptedLogLevel(org.apache.camel.LoggingLevel.WARN)
            .maximumRedeliveries(3)
            .redeliveryDelay(1000)
            .handled(true)
            .log("An unexpected error occurred: ${exception.message}");

        from("quartz://monthEndScheduler?cron=0/15+*+*+*+*+?")
            .routeId("MonthEndSchedulingForExchangeRate")
            .log("Scheduler triggered. Starting S3 file Download...")
            .process(new S3FileNameProcessor(true))
            .to("direct:S3FileDownload");

        from("direct:S3FileDownload")
            .routeId("FileDownloadFromS3")
            .to("aws2-s3://{BUCKET_NAME}?operation=getObject")
            .log("File downloaded from S3: ${header.CamelAwsS3Key}")
            .split(body().tokenize(StringUtils.LF), new ExchangeRateAggregationStrategy())
            .streaming() // Ensure memory efficiency for large files
            .bean(camelExchangeEnhancer, METHOD_SKIP_HEADERS)
            .filter(camelExchangeEnhancer::isSkipHeaders)
            .process(s3DataValidationProcessor) // Process each line
            .log("Aggregated DTO: ${body}")
            .end()
            .end()
            .to("direct:saveToDatabase");

        from("direct:saveToDatabase")
            .routeId("SaveExchangeDataToDB")
            .process(exchangeRateDataProcessor)
            .log("All data processed");
    }
}
