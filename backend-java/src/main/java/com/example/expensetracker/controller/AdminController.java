package com.example.expensetracker.controller;

import com.example.expensetracker.model.News;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.BudgetRepository;
import com.example.expensetracker.repository.CategoryRepository;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.IncomeRepository;
import com.example.expensetracker.repository.NewsRepository;
import com.example.expensetracker.repository.NotificationRepository;
import com.example.expensetracker.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final NewsRepository newsRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final BudgetRepository budgetRepository;
    private final NotificationRepository notificationRepository;

    public AdminController(UserRepository userRepository,
                           NewsRepository newsRepository,
                           CategoryRepository categoryRepository,
                           ExpenseRepository expenseRepository,
                           IncomeRepository incomeRepository,
                           BudgetRepository budgetRepository,
                           NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.newsRepository = newsRepository;
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.budgetRepository = budgetRepository;
        this.notificationRepository = notificationRepository;
    }

    private boolean isAdmin(User user) {
        return user != null && user.getRole() == User.Role.ADMIN;
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@AuthenticationPrincipal User user,
                                         @RequestParam(value = "c", required = false) String from,
                                         @RequestParam(value = "gj", required = false) String to) {
        if (!isAdmin(user)) {
            return ResponseEntity.status(403).body(Map.of("message", "Доступ запрещен. Только для администраторов."));
        }
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/users/{id}")
    @Transactional
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal User user,
                                        @PathVariable("id") Long id) {
        if (!isAdmin(user)) {
            return ResponseEntity.status(403).body(Map.of("message", "Доступ запрещен. Только для администраторов."));
        }
        return userRepository.findById(id)
                .map(u -> {
                    try {
                        notificationRepository.deleteByUser(u);
                        budgetRepository.deleteByUser(u);
                        expenseRepository.deleteByUser(u);
                        incomeRepository.deleteByUser(u);
                        categoryRepository.deleteByUser(u);
                        newsRepository.deleteByAuthor(u);
                        userRepository.delete(u);
                        return ResponseEntity.ok(Map.of("message", "Пользователь успешно удален"));
                    } catch (DataIntegrityViolationException ex) {
                        return ResponseEntity.status(409).body(Map.of(
                                "message", "Невозможно удалить пользователя, поскольку связанные записи все еще существуют.",
                                "error", ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage()
                        ));
                    }
                })
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("message", "Пользователь не найден")));
    }

    public record NewsRequest(String title, String content, String type) {}

    @PostMapping("/content")
    public ResponseEntity<?> addNews(@AuthenticationPrincipal User user,
                                     @RequestBody NewsRequest body) {
        if (!isAdmin(user)) {
            return ResponseEntity.status(403).body(Map.of("message", "Доступ запрещен. Только для администраторов."));
        }
        if (body.title() == null || body.content() == null || body.type() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Все поля обязательны для заполнения"));
        }
        News news = News.builder()
                .title(body.title())
                .content(body.content())
                .type(body.type())
                .author(user)
                .date(Instant.now())
                .build();
        newsRepository.save(news);
        return ResponseEntity.ok(news);
    }

    @GetMapping("/content/admin")
    public ResponseEntity<?> getAllContent(@AuthenticationPrincipal User user) {
        if (!isAdmin(user)) {
            return ResponseEntity.status(403).body(Map.of("message", "Доступ запрещен. Только для администраторов."));
        }
        List<News> content = newsRepository.findAll();
        return ResponseEntity.ok(content);
    }
    @PutMapping("/content/{id}")
    public ResponseEntity<?> updateNews(@AuthenticationPrincipal User user,
                                        @PathVariable("id") Long id,
                                        @RequestBody NewsRequest body) {
        if (!isAdmin(user)) {
            return ResponseEntity.status(403).body(Map.of("message", "Доступ запрещен. Только для администраторов."));
        }
        return newsRepository.findById(id)
                .map(existing -> {
                    if (body.title() != null) existing.setTitle(body.title());
                    if (body.content() != null) existing.setContent(body.content());
                    if (body.type() != null) existing.setType(body.type());
                    existing.setDate(Instant.now());
                    newsRepository.save(existing);
                    return (Object) existing;
                })
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("message", "Новость не найдена.")));
    }

    @DeleteMapping("/content/{id}")
    public ResponseEntity<?> deleteNews(@AuthenticationPrincipal User user,
                                        @PathVariable("id") Long id) {
        if (!isAdmin(user)) {
            return ResponseEntity.status(403).body(Map.of("message", "Доступ запрещен. Только для администраторов."));
        }
        return newsRepository.findById(id)
                .map(existing -> {
                    newsRepository.delete(existing);
                    return (Object) Map.of("message", "Новость успешно удалена.");
                })
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("message", "Новость не найдена.")));
    }
}