package com.example.expensetracker.integration.controller;

import com.example.expensetracker.model.Expense;
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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private IncomeRepository incomeRepository;  // ← добавляем

    @Autowired
    private BudgetRepository budgetRepository;   // ← добавляем

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
        // Важно: удаляем в правильном порядке (от зависимых к независимым)
        budgetRepository.deleteAll();    // бюджеты ссылаются на users
        expenseRepository.deleteAll();   // расходы ссылаются на users
        incomeRepository.deleteAll();    // доходы ссылаются на users
        userRepository.deleteAll();      // затем пользователей

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
    void addExpense_ShouldCreateExpense_WhenRequestIsValid() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("icon", "food-icon.png");
        request.put("category", "Еда");
        request.put("generalCategory", "Продукты");
        request.put("description", "Покупка продуктов");
        request.put("amount", 1500.00);
        request.put("date", "2025-03-27");

        mockMvc.perform(post("/api/v1/expense/add")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("Еда"))
                .andExpect(jsonPath("$.amount").value(1500.00))
                .andExpect(jsonPath("$.date").value("2025-03-27"));
    }


    @Test
    void addExpense_ShouldReturn401_WhenUserIsNotAuthenticated() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("category", "Еда");
        request.put("amount", 1500.00);
        request.put("date", "2025-03-27");

        mockMvc.perform(post("/api/v1/expense/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getExpenses_ShouldReturnPaginatedList_WhenUserIsAuthenticated() throws Exception {
        Expense expense1 = Expense.builder()
                .user(testUser)
                .category("Транспорт")
                .generalCategory("Такси")
                .amount(500.00)
                .date(LocalDate.of(2025, 3, 20))
                .build();
        Expense expense2 = Expense.builder()
                .user(testUser)
                .category("Еда")
                .generalCategory("Ресторан")
                .amount(1200.00)
                .date(LocalDate.of(2025, 3, 21))
                .build();
        expenseRepository.save(expense1);
        expenseRepository.save(expense2);

        mockMvc.perform(get("/api/v1/expense/get")
                .header("Authorization", "Bearer " + authToken)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void deleteExpense_ShouldDeleteExpense_WhenUserIsOwner() throws Exception {
        Expense expense = Expense.builder()
                .user(testUser)
                .category("Еда")
                .generalCategory("Продукты")
                .amount(1000.00)
                .date(LocalDate.now())
                .build();
        expense = expenseRepository.save(expense);

        mockMvc.perform(delete("/api/v1/expense/{id}", expense.getId())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messege").value(" Расходы успешно удалены "));
    }

    @Test
    void getExpenses_ShouldFilterByDateRange_WhenDatesAreProvided() throws Exception {
        Expense expense1 = Expense.builder()
                .user(testUser)
                .category("Транспорт")
                .amount(500.00)
                .date(LocalDate.of(2025, 3, 15))
                .build();
        Expense expense2 = Expense.builder()
                .user(testUser)
                .category("Еда")
                .amount(1200.00)
                .date(LocalDate.of(2025, 3, 25))
                .build();
        expenseRepository.save(expense1);
        expenseRepository.save(expense2);

        mockMvc.perform(get("/api/v1/expense/get")
                .header("Authorization", "Bearer " + authToken)
                .param("from", "2025-03-20")
                .param("to", "2025-03-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.items[0].category").value("Еда"));
    }
}