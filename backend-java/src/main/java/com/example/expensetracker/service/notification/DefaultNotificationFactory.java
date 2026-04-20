package com.example.expensetracker.service.notification;

import com.example.expensetracker.model.Notification;
import com.example.expensetracker.model.User;
import org.springframework.stereotype.Component;

@Component
public class DefaultNotificationFactory implements NotificationFactory {

    @Override
    public Notification createBudgetExceeded(
            User user,
            String month,
            String generalCategory,
            Double budgetAmount,
            Double spentAmount
    ) {
        return Notification.builder()
                .user(user)
                .type("BUDGET_EXCEEDED")
                .month(month)
                .generalCategory(generalCategory)
                .message("Бюджет превышен на " + generalCategory + " (" + month + "). Бюджет: "
                        + budgetAmount + ", потраченно: " + spentAmount)
                .build();
    }
}
