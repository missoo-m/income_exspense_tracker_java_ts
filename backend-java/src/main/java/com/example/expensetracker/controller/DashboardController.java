package com.example.expensetracker.controller;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.model.Income;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.IncomeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;

    public DashboardController(IncomeRepository incomeRepository, ExpenseRepository expenseRepository) {
        this.incomeRepository = incomeRepository;
        this.expenseRepository = expenseRepository;
    }

    @GetMapping
    public ResponseEntity<?> getDashboard(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("messege", "Unauthorized"));
        }

        Double totalIncome = Optional.ofNullable(incomeRepository.sumByUser(user)).orElse(0.0);
        Double totalExpense = Optional.ofNullable(expenseRepository.sumByUser(user)).orElse(0.0);

        LocalDate now = LocalDate.now();
        LocalDate fromIncome = now.minusDays(60);
        LocalDate fromExpense = now.minusDays(30);

        List<Income> last60Income = incomeRepository
                .findByUserAndDateGreaterThanEqualOrderByDateDesc(user, fromIncome);
        double incomeLast60 = last60Income.stream().mapToDouble(Income::getAmount).sum();

        List<Expense> last30Expense = expenseRepository
                .findByUserAndDateGreaterThanEqualOrderByDateDesc(user, fromExpense);
        double expenseLast30 = last30Expense.stream().mapToDouble(Expense::getAmount).sum();

        // Группировка всех расходов по категориям для круговой диаграммы
        List<Expense> allExpenses = expenseRepository.findByUserOrderByDateDesc(user);
        Map<String, Double> byCategory = allExpenses.stream()
                .collect(Collectors.groupingBy(
                        e -> Optional.ofNullable(e.getCategory()).orElse("Uncategorized"),
                        Collectors.summingDouble(Expense::getAmount)
                ));
        List<Map<String, Object>> expensesByCategory = byCategory.entrySet().stream()
                .map(en -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("category", en.getKey());
                    m.put("total", en.getValue());
                    return m;
                })
                .toList();

        List<Map<String, Object>> recent = new ArrayList<>();
        incomeRepository.findByUserOrderByDateDesc(user).stream().limit(5).forEach(i -> {
            Map<String, Object> m = new HashMap<>();
            m.putAll(Map.of(
                    "_id", i.getId(),
                    "amount", i.getAmount(),
                    "date", i.getDate(),
                    "icon", i.getIcon(),
                    "source", i.getSource(),
                    "type", "income"
            ));
            recent.add(m);
        });
        expenseRepository.findByUserOrderByDateDesc(user).stream().limit(5).forEach(e -> {
            Map<String, Object> m = new HashMap<>();
            m.putAll(Map.of(
                    "_id", e.getId(),
                    "amount", e.getAmount(),
                    "date", e.getDate(),
                    "icon", e.getIcon(),
                    "category", e.getCategory(),
                    "type", "expense"
            ));
            recent.add(m);
        });
        recent.sort((a, b) -> {
            Comparable da = (Comparable) a.get("date");
            Comparable db = (Comparable) b.get("date");
            return db.compareTo(da);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("totalBalance", totalIncome - totalExpense);
        response.put("totalIncome", totalIncome);
        response.put("totalExpense", totalExpense);
        response.put("last30DaysExpenses", Map.of(
                "total", expenseLast30,
                "transactions", last30Expense
        ));
        response.put("last60DaysIncome", Map.of(
                "total", incomeLast60,
                "transactions", last60Income
        ));
        response.put("recentTransactions", recent);
        response.put("expensesByCategory", expensesByCategory);

        return ResponseEntity.ok(response);
    }
}

