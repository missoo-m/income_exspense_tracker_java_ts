package com.example.expensetracker.dto;

import com.example.expensetracker.model.User;

public record NewsDto(
        String title,
        String content,
        String type,
        User author
) {
}
