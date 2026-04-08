package com.example.expensetracker.dto;

import java.util.Map;

public class CurrencyRatesDto {
    private String baseCurrency;
    private Map<String, Double> rates;
    private String source;
    private String date;

    // Пустой конструктор ОБЯЗАТЕЛЕН для Jackson
    public CurrencyRatesDto() {}

    public CurrencyRatesDto(String baseCurrency, Map<String, Double> rates, String source, String date) {
        this.baseCurrency = baseCurrency;
        this.rates = rates;
        this.source = source;
        this.date = date;
    }

    public String getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(String baseCurrency) { this.baseCurrency = baseCurrency; }

    public Map<String, Double> getRates() { return rates; }
    public void setRates(Map<String, Double> rates) { this.rates = rates; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}