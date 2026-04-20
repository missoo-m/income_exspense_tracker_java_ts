package com.example.expensetracker.integration.controller;

import com.example.expensetracker.model.Income;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IncomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private IncomeRepository incomeRepository;

    @Autowired
    private ExpenseRepository expenseRepository;  // ← добавляем

    @Autowired
    private BudgetRepository budgetRepository;    // ← добавляем

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private String authToken;

    @BeforeEach
    void setUp() {
        // Важно: удаляем в правильном порядке
        budgetRepository.deleteAll();    // бюджеты
        expenseRepository.deleteAll();   // расходы
        incomeRepository.deleteAll();    // доходы
        userRepository.deleteAll();      // пользователей

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
    void addIncome_ShouldCreateIncome_WhenRequestIsValid() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("icon", "salary-icon.png");
        request.put("source", "Зарплата");
        request.put("amount", 50000.00);
        request.put("date", "2025-03-27");

        mockMvc.perform(post("/api/v1/income/add")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source").value("Зарплата"))
                .andExpect(jsonPath("$.amount").value(50000.00))
                .andExpect(jsonPath("$.date").value("2025-03-27"));
    }

    @Test
    void addIncome_ShouldReturn401_WhenUserIsNotAuthenticated() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("source", "Зарплата");
        request.put("amount", 50000.00);
        request.put("date", "2025-03-27");

        mockMvc.perform(post("/api/v1/income/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getIncomes_ShouldReturnPaginatedList_WhenUserIsAuthenticated() throws Exception {
        Income income1 = Income.builder()
                .user(testUser)
                .source("Зарплата")
                .amount(50000.00)
                .date(LocalDate.of(2025, 3, 20))
                .build();
        Income income2 = Income.builder()
                .user(testUser)
                .source("Фриланс")
                .amount(15000.00)
                .date(LocalDate.of(2025, 3, 21))
                .build();
        incomeRepository.save(income1);
        incomeRepository.save(income2);

        mockMvc.perform(get("/api/v1/income/get")
                .header("Authorization", "Bearer " + authToken)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void deleteIncome_ShouldDeleteIncome_WhenUserIsOwner() throws Exception {
        Income income = Income.builder()
                .user(testUser)
                .source("Зарплата")
                .amount(50000.00)
                .date(LocalDate.now())
                .build();
        income = incomeRepository.save(income);

        mockMvc.perform(delete("/api/v1/income/{id}", income.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messege").value(" Доход успешно удален "));
    }

    @Test
    void getIncomes_ShouldFilterBySource_WhenSourceIsProvided() throws Exception {
        Income income1 = Income.builder()
                .user(testUser)
                .source("Зарплата")
                .amount(50000.00)
                .date(LocalDate.of(2025, 3, 20))
                .build();
        Income income2 = Income.builder()
                .user(testUser)
                .source("Фриланс")
                .amount(15000.00)
                .date(LocalDate.of(2025, 3, 21))
                .build();
        incomeRepository.save(income1);
        incomeRepository.save(income2);

        mockMvc.perform(get("/api/v1/income/get")
                .header("Authorization", "Bearer " + authToken)
                .param("source", "Зарплата"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.items[0].source").value("Зарплата"));
    }
}