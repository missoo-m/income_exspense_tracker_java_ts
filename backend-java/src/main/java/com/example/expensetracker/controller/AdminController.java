package com.example.expensetracker.controller;

import com.example.expensetracker.dto.NewsDto;
import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.model.News;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.BudgetRepository;
import com.example.expensetracker.repository.CategoryRepository;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.IncomeRepository;
import com.example.expensetracker.repository.NewsRepository;
import com.example.expensetracker.repository.NotificationRepository;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.service.NewsCrudService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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
    private final NewsCrudService newsCrudService;

    public AdminController(UserRepository userRepository,
                           NewsRepository newsRepository,
                           CategoryRepository categoryRepository,
                           ExpenseRepository expenseRepository,
                           IncomeRepository incomeRepository,
                           BudgetRepository budgetRepository,
                           NotificationRepository notificationRepository,
                           NewsCrudService newsCrudService) {
        this.userRepository = userRepository;
        this.newsRepository = newsRepository;
        this.categoryRepository = categoryRepository;
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.budgetRepository = budgetRepository;
        this.notificationRepository = notificationRepository;
        this.newsCrudService = newsCrudService;
    }

    private boolean isAdmin(User user) {
        return user != null && user.getRole() == User.Role.ADMIN;
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@AuthenticationPrincipal User user,
                                        @PathVariable("id") Long id) {
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addNews(@AuthenticationPrincipal User user,
                                     @RequestBody NewsRequest body) {
        if (body.title() == null || body.content() == null || body.type() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Все поля обязательны для заполнения"));
        }
        News news = newsCrudService.create(new NewsDto(
                body.title(),
                body.content(),
                body.type(),
                user
        ));
        return ResponseEntity.ok(news);
    }

    @GetMapping("/content/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllContent(@AuthenticationPrincipal User user) {
        List<News> content = newsCrudService.findAll();
        return ResponseEntity.ok(content);
    }
    @PutMapping("/content/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateNews(@AuthenticationPrincipal User user,
                                        @PathVariable("id") Long id,
                                        @RequestBody NewsRequest body) {
        try {
            News updated = newsCrudService.update(id, new NewsDto(
                    body.title(),
                    body.content(),
                    body.type(),
                    null
            ));
            return ResponseEntity.ok(updated);
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(404).body(Map.of("message", "Новость не найдена."));
        }
    }

    @DeleteMapping("/content/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteNews(@AuthenticationPrincipal User user,
                                        @PathVariable("id") Long id) {
        try {
            newsCrudService.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Новость успешно удалена."));
        } catch (ResourceNotFoundException ex) {
            return ResponseEntity.status(404).body(Map.of("message", "Новость не найдена."));
        }
    }
}