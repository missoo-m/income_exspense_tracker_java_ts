package com.example.expensetracker.unit.service;

import com.example.expensetracker.service.CurrencyRateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {
        CurrencyRateService.class,
        CurrencyRateServiceTest.TestCacheConfig.class
})
class CurrencyRateServiceTest {

    @Autowired
    private CurrencyRateService currencyRateService;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private RestTemplate restTemplate;

    @BeforeEach
    void clearCache() {
        cacheManager.getCache(CurrencyRateService.CURRENCY_RATES_CACHE).clear();
    }

    @Test
    void getCurrentRatesCached_ShouldReturnRates() {
        CurrencyRateService.NbrbRate[] apiResponse = new CurrencyRateService.NbrbRate[]{
                new CurrencyRateService.NbrbRate("USD", 3.25, 1),
                new CurrencyRateService.NbrbRate("EUR", 3.45, 1),
                new CurrencyRateService.NbrbRate("RUB", 3.55, 100),
                new CurrencyRateService.NbrbRate("JPY", 2.40, 100)
        };
        when(restTemplate.getForObject(eq("https://api.nbrb.by/exrates/rates?periodicity=0"), eq(CurrencyRateService.NbrbRate[].class)))
                .thenReturn(apiResponse);

        Map<String, Object> result = currencyRateService.getCurrentRatesCached();

        assertThat(result).isNotNull();
        assertThat(result.get("baseCurrency")).isEqualTo("BYN");
        assertThat(result.get("source")).isEqualTo("NBRB");
        assertThat(result.get("rates")).isNotNull();
    }

    @Test
    void getCurrentRatesCached_ShouldCallApiOnlyOnce_WhenValueIsCached() {
        CurrencyRateService.NbrbRate[] apiResponse = new CurrencyRateService.NbrbRate[]{
                new CurrencyRateService.NbrbRate("USD", 3.20, 1)
        };
        when(restTemplate.getForObject(eq("https://api.nbrb.by/exrates/rates?periodicity=0"), eq(CurrencyRateService.NbrbRate[].class)))
                .thenReturn(apiResponse);

        Map<String, Object> firstCall = currencyRateService.getCurrentRatesCached();
        Map<String, Object> secondCall = currencyRateService.getCurrentRatesCached();

        assertThat(firstCall).isEqualTo(secondCall);
        verify(restTemplate, times(1))
                .getForObject(eq("https://api.nbrb.by/exrates/rates?periodicity=0"), eq(CurrencyRateService.NbrbRate[].class));
    }

    @TestConfiguration
    @EnableCaching
    static class TestCacheConfig {
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(CurrencyRateService.CURRENCY_RATES_CACHE);
        }
    }
}
