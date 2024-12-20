package com.example.demo.camel.service;

import org.apache.camel.Exchange;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

@Component
public class CamelExchangeEnhancer {

    public static final String STARTING_HEADER_FIELD = "EFFECTIVE_DATE";
    public static final String EXCHANGE_PROPERTY_SKIP_LINES = "SKIP_LINE";

    public void skipHeaders(Exchange exchange) {

        final String line = exchange.getIn().getBody(String.class);

        if (ObjectUtils.isNotEmpty(line) && line.startsWith(STARTING_HEADER_FIELD)) {
            exchange.setProperty(EXCHANGE_PROPERTY_SKIP_LINES, true);
        } else {
            exchange.setProperty(EXCHANGE_PROPERTY_SKIP_LINES, false);
        }
    }

    public boolean isSkipHeaders(Exchange exchange) {
        return exchange.getProperty(EXCHANGE_PROPERTY_SKIP_LINES, Boolean.class).equals(false);
    }
}
