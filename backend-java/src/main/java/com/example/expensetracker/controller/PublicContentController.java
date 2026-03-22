package com.example.expensetracker.controller;

import com.example.expensetracker.model.News;
import com.example.expensetracker.repository.NewsRepository;
import com.example.expensetracker.service.CurrencyRateService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public")
public class PublicContentController {

    private final NewsRepository newsRepository;
    private final CurrencyRateService currencyRateService;

    public PublicContentController(NewsRepository newsRepository, CurrencyRateService currencyRateService) {
        this.newsRepository = newsRepository;
        this.currencyRateService = currencyRateService;
    }

    // DTO под ответ NBRB
    public record NbrbRate(
            String Cur_Abbreviation,
            Double Cur_OfficialRate,
            Integer Cur_Scale
    ) {}

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
        try {
            Map<String, Object> payload = currencyRateService.getCurrentRatesCached();
            return ResponseEntity.ok(payload);
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of(
                    "message", "Не удалось загрузить курсы валют из НБРБ.",
                    "error", ex.getMessage()
            ));
        }
    }
}

