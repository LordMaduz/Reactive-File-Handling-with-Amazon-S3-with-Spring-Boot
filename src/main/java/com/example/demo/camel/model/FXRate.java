package com.example.demo.camel.model;


import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class FXRate {

    private String fromCurrency;
    private String toCurrency;
    private double exchangeRate;

}
