package com.example.expensetracker.testutil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TestAuth {

    public static String registerAndLogin(MockMvc mvc, ObjectMapper om, String email) throws Exception {
        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Test User",
                                  "email": "%s",
                                  "password": "password123",
                                  "role": "NORMAL"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated());

        var loginRes = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "%s", "password": "password123" }
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = om.readTree(loginRes.getResponse().getContentAsString());
        return json.get("token").asText();
    }
}

