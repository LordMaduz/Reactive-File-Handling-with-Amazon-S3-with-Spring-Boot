package com.example.demo.camel.collection;

import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.example.demo.camel.model.FXRate;

import lombok.Data;

@Document
@Data
public class ExchangeRates {

    @Id
    private String id;
    private String rateCode;
    private String rateSource;
    private String rateType;
    private String countryCode;
    private String businessDate;
    private String accountingYear;
    private Set<FXRate> rates;

}
