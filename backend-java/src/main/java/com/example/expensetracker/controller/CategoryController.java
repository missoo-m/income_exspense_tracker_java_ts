package com.example.expensetracker.controller;

import com.example.expensetracker.model.Category;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.CategoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public record CategoryRequest(String name) {}

    @GetMapping
    public ResponseEntity<?> getAll(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        // Если у пользователя еще нет категорий — создаём базовый набор
        if (categoryRepository.countByUser(user) == 0) {
            List<String> defaults = Arrays.asList(
                    "Транспорт",
                    "Еда",
                    "Дом",
                    "Здоровье",
                    "Развлечение",
                    "Шоппинг",
                    "Образование",
                    "Другое"
            );
            defaults.forEach(name -> {
                if (!categoryRepository.existsByUserAndName(user, name)) {
                    categoryRepository.save(Category.builder()
                            .user(user)
                            .name(name)
                            .isDefault(true)
                            .build());
                }
            });
        }

        List<Category> categories = categoryRepository.findByUserOrderByNameAsc(user);
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    public ResponseEntity<?> create(@AuthenticationPrincipal User user,
                                    @RequestBody CategoryRequest body) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        String name = body.name();
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Укажите название категории."));
        }
        if (categoryRepository.existsByUserAndName(user, name.trim())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Категория с таким названием уже существует."));
        }
        Category category = Category.builder()
                .user(user)
                .name(name.trim())
                .isDefault(false)
                .build();
        categoryRepository.save(category);
        return ResponseEntity.ok(category);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@AuthenticationPrincipal User user,
                                    @PathVariable("id") Long id,
                                    @RequestBody CategoryRequest body) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        String name = body.name();
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Укажите название категории."));
        }
        return categoryRepository.findByIdAndUser(id, user)
                .map(existing -> {
                    String trimmed = name.trim();
                    if (!existing.getName().equals(trimmed)
                            && categoryRepository.existsByUserAndName(user, trimmed)) {
                        return ResponseEntity.badRequest()
                                .body(Map.of("message", "Категория с таким названием уже существует."));
                    }
                    existing.setName(trimmed);
                    categoryRepository.save(existing);
                    return ResponseEntity.ok(existing);
                })
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("message", "Категория не найдена")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@AuthenticationPrincipal User user,
                                    @PathVariable("id") Long id) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        return categoryRepository.findByIdAndUser(id, user)
                .map(existing -> {
                    categoryRepository.delete(existing);
                    return ResponseEntity.ok(Map.of("message", "Категория успешно удалена"));
                })
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("message", "Категория не найдена")));
    }
}