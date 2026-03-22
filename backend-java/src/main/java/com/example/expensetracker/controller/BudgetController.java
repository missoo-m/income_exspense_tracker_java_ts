package com.example.expensetracker.controller;

import com.example.expensetracker.model.Budget;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.BudgetRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/budgets")
public class BudgetController {

    private final BudgetRepository budgetRepository;

    public BudgetController(BudgetRepository budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    public record BudgetRequest(
            @NotBlank String month, // YYYY-MM
            @NotBlank String generalCategory,
            @NotNull Double amount
    ) {}

    @GetMapping
    public ResponseEntity<?> getByMonth(@AuthenticationPrincipal User user,
                                        @RequestParam("month") String month) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Несанкционирован"));
        }
        String m = Budget.normalizeMonth(month);
        List<Budget> budgets = budgetRepository.findByUserAndMonthOrderByGeneralCategoryAsc(user, m);
        return ResponseEntity.ok(budgets);
    }

    @PostMapping
    public ResponseEntity<?> upsert(@AuthenticationPrincipal User user,
                                    @RequestBody BudgetRequest body) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        String month = Budget.normalizeMonth(body.month());
        String category = body.generalCategory().trim();
        if (body.amount() == null || body.amount() < 0) {
            return ResponseEntity.badRequest().body(Map.of("message", "Неверная сумма"));
        }

        Budget budget = budgetRepository.findByUserAndMonthAndGeneralCategory(user, month, category)
                .orElseGet(() -> Budget.builder()
                        .user(user)
                        .month(month)
                        .generalCategory(category)
                        .build());
        budget.setAmount(body.amount());
        budgetRepository.save(budget);
        return ResponseEntity.ok(budget);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@AuthenticationPrincipal User user,
                                    @PathVariable Long id) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        return budgetRepository.findByIdAndUser(id, user)
                .map(b -> {
                    budgetRepository.delete(b);
                    return ResponseEntity.ok(Map.of("message", "Бюджет удален."));
                })
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("message", "Бюджет не найден")));
    }
}

