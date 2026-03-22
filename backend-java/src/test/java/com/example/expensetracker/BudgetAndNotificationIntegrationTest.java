package com.example.expensetracker;

import com.example.expensetracker.config.TestOAuth2Config;
import com.example.expensetracker.testutil.TestAuth;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestOAuth2Config.class)  // ← ДОБАВЬ ЭТУ СТРОКУ
class BudgetAndNotificationIntegrationTest {

    @Autowired
    private MockMvc mvc;
    
    @Autowired
    private ObjectMapper om;

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        token = TestAuth.registerAndLogin(mvc, om, "budget-" + System.currentTimeMillis() + "@example.com");
    }

    @Test
    void adding_expense_over_budget_creates_notification() throws Exception {
        // Устанавливаем бюджет
        mvc.perform(post("/api/v1/budgets")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "month": "2026-03",
                                  "generalCategory": "Transport",
                                  "amount": 10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.generalCategory").value("Transport"));

        // Добавляем расход, превышающий бюджет
        mvc.perform(post("/api/v1/expense/add")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "generalCategory": "Transport",
                                  "description": "Taxi",
                                  "amount": 20,
                                  "date": "2026-03-10"
                                }
                                """))
                .andExpect(status().isOk());

        // Проверяем уведомление
        mvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].type").value("BUDGET_EXCEEDED"));
    }
}