package com.example.expensetracker;

import com.example.expensetracker.config.TestOAuth2Config;
import com.example.expensetracker.testutil.TestAuth;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class CategoryIntegrationTest {

    @Autowired
    private MockMvc mvc;
    
    @Autowired
    private ObjectMapper om;

    @Test
    void getCategories_creates_defaults_for_user() throws Exception {
        String token = TestAuth.registerAndLogin(mvc, om, "cat@example.com");

        mvc.perform(get("/api/v1/categories")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    void createCategory_works() throws Exception {
        String token = TestAuth.registerAndLogin(mvc, om, "cat2@example.com");

        mvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"My Custom\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("My Custom"));
    }
}