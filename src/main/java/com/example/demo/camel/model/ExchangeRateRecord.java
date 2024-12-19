package com.example.demo.camel.model;

import java.time.LocalDate;


public record ExchangeRateRecord(LocalDate effectiveDate, String fromCurrency, String toCurrency, String rateType, Double rateMultiplier, Integer rateDiv,
                                 String lastUpdatedDateTime, String dataOriginCountry, String dataBusinessUnit, LocalDate businessDate) {

}
