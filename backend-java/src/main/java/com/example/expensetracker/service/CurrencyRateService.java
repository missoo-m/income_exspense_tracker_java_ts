package com.example.expensetracker.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CurrencyRateService {

    public record NbrbRate(
            String Cur_Abbreviation,
            Double Cur_OfficialRate,
            Integer Cur_Scale
    ) {}

    //@Cacheable(cacheNames = "nbrbRates", key = "'periodicity0'")
    public Map<String, Object> getCurrentRatesCached() {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.nbrb.by/exrates/rates?periodicity=0";
        NbrbRate[] rates = restTemplate.getForObject(url, NbrbRate[].class);

        Map<String, Double> mappedRates = new HashMap<>();
        if (rates != null) {
            for (NbrbRate r : rates) {
                if (r == null || r.Cur_Abbreviation() == null || r.Cur_OfficialRate() == null || r.Cur_Scale() == null) {
                    continue;
                }
                String code = r.Cur_Abbreviation();
                if (!List.of("USD", "EUR", "RUB", "JPY").contains(code)) {
                    continue;
                }
                double perUnit = r.Cur_OfficialRate() / r.Cur_Scale();
                mappedRates.put(code.equals("JPY") ? "YEN" : code, perUnit);
            }
        }

        return Map.of(
                "baseCurrency", "BYN",
                "rates", mappedRates,
                "date", Instant.now().toString(),
                "source", "NBRB"
        );
    }
}

