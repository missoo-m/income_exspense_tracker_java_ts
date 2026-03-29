package com.example.expensetracker.integration.controller;

import com.example.expensetracker.model.Budget;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.BudgetRepository;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.IncomeRepository;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.security.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private ExpenseRepository expenseRepository;  // ← добавляем

    @Autowired
    private IncomeRepository incomeRepository;    // ← добавляем

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private String authToken;
    private String currentMonth;

    @BeforeEach
    void setUp() {
        // Важно: удаляем в правильном порядке
        budgetRepository.deleteAll();    // бюджеты
        expenseRepository.deleteAll();   // расходы
        incomeRepository.deleteAll();    // доходы
        userRepository.deleteAll();      // пользователей

        currentMonth = YearMonth.now().toString();

        testUser = User.builder()
                .fullName("Тестовый Пользователь")
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(User.Role.NORMAL)
                .build();
        testUser = userRepository.save(testUser);
        authToken = jwtService.generateToken(testUser.getId());
    }

    @Test
    void upsertBudget_ShouldCreateNewBudget_WhenBudgetDoesNotExist() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("month", currentMonth);
        request.put("generalCategory", "Продукты");
        request.put("amount", 1500.00);

        mockMvc.perform(post("/api/v1/budgets")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value(currentMonth))
                .andExpect(jsonPath("$.generalCategory").value("Продукты"))
                .andExpect(jsonPath("$.amount").value(1500.00));
    }

    @Test
    void upsertBudget_ShouldUpdateExistingBudget_WhenBudgetExists() throws Exception {
        Budget existingBudget = Budget.builder()
                .user(testUser)
                .month(currentMonth)
                .generalCategory("Транспорт")
                .amount(500.00)
                .build();
        budgetRepository.save(existingBudget);

        Map<String, Object> request = new HashMap<>();
        request.put("month", currentMonth);
        request.put("generalCategory", "Транспорт");
        request.put("amount", 800.00);

        mockMvc.perform(post("/api/v1/budgets")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(800.00));
    }

    @Test
    void getBudgetsByMonth_ShouldReturnBudgets_WhenUserHasBudgets() throws Exception {
        Budget budget1 = Budget.builder()
                .user(testUser)
                .month(currentMonth)
                .generalCategory("Продукты")
                .amount(1500.00)
                .build();
        Budget budget2 = Budget.builder()
                .user(testUser)
                .month(currentMonth)
                .generalCategory("Транспорт")
                .amount(500.00)
                .build();
        budgetRepository.save(budget1);
        budgetRepository.save(budget2);

        mockMvc.perform(get("/api/v1/budgets")
                .header("Authorization", "Bearer " + authToken)
                .param("month", currentMonth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void deleteBudget_ShouldDeleteBudget_WhenUserIsOwner() throws Exception {
        Budget budget = Budget.builder()
                .user(testUser)
                .month(currentMonth)
                .generalCategory("Продукты")
                .amount(1500.00)
                .build();
        budget = budgetRepository.save(budget);

        mockMvc.perform(delete("/api/v1/budgets/{id}", budget.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Бюджет удален."));
    }
}