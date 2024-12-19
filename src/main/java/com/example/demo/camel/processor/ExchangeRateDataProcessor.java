package com.example.demo.camel.processor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.util.CollectionUtils;

import com.example.demo.camel.collection.ExchangeRates;
import com.example.demo.camel.model.ExchangeRateRecord;
import com.example.demo.camel.model.FXRate;
import com.example.demo.camel.repo.ExchangeRatesRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExchangeRateDataProcessor implements Processor {

    private final ExchangeRatesRepository exchangeRatesRepository;

    @Override
    public void process(Exchange exchange)  {

        final List<ExchangeRateRecord> exchangeRateRecords = exchange.getIn().getBody(List.class);

        if (CollectionUtils.isEmpty(exchangeRateRecords)) {
            throw new IllegalArgumentException("No valid data to save to MongoDB.");
        }

        final Set<FXRate> fxRates = exchangeRateRecords.stream()
            .map(record-> new FXRate(record.fromCurrency(), record.toCurrency(), record.rateMultiplier()))
            .collect(Collectors.toSet());

        final String rateCode = "CRRNT";
        final String rateSource = "PSGL";
        final String rateType = exchangeRateRecords.get(0).rateType();
        final String countryCode = "RG";
        final LocalDate businessDate = exchangeRateRecords.get(0).businessDate();

        Optional<ExchangeRates> exchangeRatesOptional = exchangeRatesRepository.findByRateCodeAndRateSourceAndRateTypeAndCountryCodeAndBusinessDate(
            rateCode, rateSource, rateType, countryCode, businessDate.toString()
        );

        exchangeRatesOptional.ifPresentOrElse(exchangeRates-> {
            exchangeRates.setRates(fxRates);
            exchangeRatesRepository.save(exchangeRates);
        }, ()-> {
            ExchangeRates exchangeRates = new ExchangeRates();
            exchangeRates.setRateCode(rateCode);
            exchangeRates.setRateSource(rateSource);
            exchangeRates.setRateType(rateType);
            exchangeRates.setCountryCode(countryCode);
            exchangeRates.setBusinessDate(businessDate.toString());
            exchangeRates.setAccountingYear(String.valueOf(businessDate.getYear()));
            exchangeRates.setRates(fxRates);

            exchangeRatesRepository.save(exchangeRates);
        });
    }
}