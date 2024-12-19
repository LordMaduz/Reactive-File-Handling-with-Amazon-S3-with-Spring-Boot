package com.example.demo.camel.aggregation;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePropertyKey;
import org.apache.camel.processor.aggregate.AbstractListAggregationStrategy;

import com.example.demo.camel.model.ExchangeRateRecord;

public class ExchangeRateAggregationStrategy extends AbstractListAggregationStrategy<Object> {

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        List<ExchangeRateRecord> list;

        if (oldExchange == null) {
            list = getList(newExchange);
        } else {
            list = getList(oldExchange);
        }

        if (newExchange != null) {
            Object value = getValue(newExchange);
            if (value instanceof ExchangeRateRecord exchangeRateRecord) {
                list.add(exchangeRateRecord);
            }
        }

        return oldExchange != null ? oldExchange : newExchange;
    }

    @SuppressWarnings("unchecked")
    private List<ExchangeRateRecord> getList(Exchange exchange) {
        List<ExchangeRateRecord> list = exchange.getProperty(ExchangePropertyKey.GROUPED_EXCHANGE, List.class);
        if (list == null) {
            list = new GroupedExchangeList<>();
            exchange.setProperty(ExchangePropertyKey.GROUPED_EXCHANGE, list);
        }
        return list;
    }

    @Override
    public Object getValue(Exchange exchange) {
        return exchange.getIn().getBody();
    }

    private static final class GroupedExchangeList<E> extends ArrayList<E> {

        private static final @Serial long serialVersionUID = 1L;

        @Override
        public String toString() {
            // override toString, so we don't write data for all the Exchanges by default
            return "List<Exchange>(" + size() + " elements)";
        }
    }
}
