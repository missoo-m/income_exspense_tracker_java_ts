package com.example.expensetracker.controller;

import com.example.expensetracker.dto.CurrencyRatesDto;
import com.example.expensetracker.model.News;
import com.example.expensetracker.repository.NewsRepository;
import com.example.expensetracker.service.CurrencyRateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public")
public class PublicContentController {

    private static final Logger log = LoggerFactory.getLogger(PublicContentController.class);

    private final NewsRepository newsRepository;
    private final CurrencyRateService currencyRateService;

    public PublicContentController(NewsRepository newsRepository, CurrencyRateService currencyRateService) {
        this.newsRepository = newsRepository;
        this.currencyRateService = currencyRateService;
    }

    @GetMapping("/news")
    public ResponseEntity<?> getPublicNews() {
        List<News> news = newsRepository.findByTypeOrderByDateDesc("news")
                .stream()
                .limit(10)
                .toList();
        return ResponseEntity.ok(news);
    }

    @GetMapping("/currencies")
public ResponseEntity<?> getCurrentCurrencies() {
    log.info(">>> КОНТРОЛЛЕР: запрос курсов валют <<<");
    try {
        CurrencyRatesDto payload = currencyRateService.getCurrentRatesCached();
        log.info(">>> КОНТРОЛЛЕР: данные получены, отправляем ответ <<<");
        return ResponseEntity.ok(payload);
    } catch (Exception ex) {
        log.error(">>> КОНТРОЛЛЕР: ошибка - {}", ex.getMessage());
        return ResponseEntity.status(500).body(Map.of(
                "message", "Не удалось загрузить курсы валют из НБРБ.",
                "error", ex.getMessage()
        ));
    }
}
}