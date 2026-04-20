package com.example.expensetracker.service.notification;

import com.example.expensetracker.model.Notification;
import com.example.expensetracker.model.User;

public interface NotificationFactory {
    Notification createBudgetExceeded(User user, String month, String generalCategory, Double budgetAmount, Double spentAmount);
}
