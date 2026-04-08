package com.example.expensetracker.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CurrencyRateService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyRateService.class);
    public static final String CURRENCY_RATES_CACHE = "currencyRates";
    private static final String NBRB_CACHE_KEY = "nbrb";
    private static final String NBRB_RATES_URL = "https://api.nbrb.by/exrates/rates?periodicity=0";

    private final RestTemplate restTemplate;

    public CurrencyRateService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public record NbrbRate(
            String Cur_Abbreviation,
            Double Cur_OfficialRate,
            Integer Cur_Scale
    ) {}

    @Cacheable(value = CURRENCY_RATES_CACHE, key = "'" + NBRB_CACHE_KEY + "'", sync = true)
    public Map<String, Object> getCurrentRatesCached() {
        System.out.println("!!! СЕРВИС ВЫЗВАН (НЕ ИЗ КЭША) !!!");
        log.info(">>> ВЫЗОВ API (НЕ ИЗ КЭША) - ЗАГРУЖАЮ СВЕЖИЕ ДАННЫЕ <<<");

        try {
            NbrbRate[] rates = restTemplate.getForObject(NBRB_RATES_URL, NbrbRate[].class);

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

            Map<String, Object> result = Map.of(
                    "baseCurrency", "BYN",
                    "rates", mappedRates,
                    "date", Instant.now().toString(),
                    "source", "NBRB"
            );
            
            log.info(">>> ДАННЫЕ ЗАГРУЖЕНЫ И БУДУТ СОХРАНЕНЫ В КЭШ <<<");
            return result;
            
        } catch (Exception e) {
            log.error("Ошибка при загрузке курсов: {}", e.getMessage());
            throw new RuntimeException("Не удалось загрузить курсы валют", e);
        }
    }
}