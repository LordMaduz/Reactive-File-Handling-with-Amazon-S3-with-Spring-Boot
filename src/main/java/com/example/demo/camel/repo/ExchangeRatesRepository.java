package com.example.demo.camel.repo;


import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.camel.collection.ExchangeRates;

@Repository
public interface ExchangeRatesRepository extends MongoRepository<ExchangeRates, String> {

    Optional<ExchangeRates> findByRateCodeAndRateSourceAndRateTypeAndCountryCodeAndBusinessDate(
        final String rateCode,
        final String rateSource,
        final String rateType,
        final String countryCode,
        final String businessDate
    );

}

