package com.example.expensetracker.service.budget;

import com.example.expensetracker.model.Budget;

public interface BudgetNotificationStrategy {
    boolean shouldNotify(Budget budget, Double spentAmount);
}
