package com.example.expensetracker.service.budget;

import com.example.expensetracker.model.Budget;
import org.springframework.stereotype.Component;

@Component
public class BudgetExceededStrategy implements BudgetNotificationStrategy {
    @Override
    public boolean shouldNotify(Budget budget, Double spentAmount) {
        return spentAmount != null && budget != null && spentAmount > budget.getAmount();
    }
}
