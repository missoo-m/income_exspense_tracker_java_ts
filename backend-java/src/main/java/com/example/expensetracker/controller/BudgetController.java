package com.example.expensetracker.controller;

import com.example.expensetracker.exception.BadRequestException;
import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.model.Budget;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.BudgetRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getByMonth(@AuthenticationPrincipal User user,
                                        @RequestParam("month") String month) {
        String m = Budget.normalizeMonth(month);
        List<Budget> budgets = budgetRepository.findByUserAndMonthOrderByGeneralCategoryAsc(user, m);
        return ResponseEntity.ok(budgets);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> upsert(@AuthenticationPrincipal User user,
                                    @RequestBody BudgetRequest body) {
        String month = Budget.normalizeMonth(body.month());
        String category = body.generalCategory().trim();
        if (body.amount() == null || body.amount() < 0) {
            throw new BadRequestException("Неверная сумма");
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> delete(@AuthenticationPrincipal User user,
                                    @PathVariable("id") Long id) {
        return budgetRepository.findByIdAndUser(id, user)
                .map(b -> {
                    budgetRepository.delete(b);
                    return ResponseEntity.ok(Map.of("message", "Бюджет удален."));
                })
                .orElseThrow(() -> new ResourceNotFoundException("Бюджет не найден"));
    }
}

