package com.example.expensetracker.service;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.model.Notification;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.BudgetRepository;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.NotificationRepository;
import com.example.expensetracker.service.budget.BudgetNotificationStrategy;
import com.example.expensetracker.service.notification.NotificationFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
public class BudgetAlertService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final NotificationRepository notificationRepository;
    private final BudgetNotificationStrategy budgetNotificationStrategy;
    private final NotificationFactory notificationFactory;

    public BudgetAlertService(
            BudgetRepository budgetRepository,
            ExpenseRepository expenseRepository,
            NotificationRepository notificationRepository,
            BudgetNotificationStrategy budgetNotificationStrategy,
            NotificationFactory notificationFactory
    ) {
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
        this.notificationRepository = notificationRepository;
        this.budgetNotificationStrategy = budgetNotificationStrategy;
        this.notificationFactory = notificationFactory;
    }

    public void checkBudgetAndNotify(User user, Expense expense) {
        String generalCategory = expense.getGeneralCategory();
        String month = YearMonth.from(expense.getDate()).toString();

        budgetRepository.findByUserAndMonthAndGeneralCategory(user, month, generalCategory)
                .ifPresent(budget -> {
                    LocalDate from = YearMonth.parse(month).atDay(1);
                    LocalDate to = YearMonth.parse(month).atEndOfMonth();
                    Double spent = expenseRepository.sumByUserAndGeneralCategoryAndMonth(user, generalCategory, from, to);

                    if (!budgetNotificationStrategy.shouldNotify(budget, spent)) {
                        return;
                    }

                    boolean exists = notificationRepository.existsByUserAndTypeAndMonthAndGeneralCategory(
                            user, "BUDGET_EXCEEDED", month, generalCategory
                    );
                    if (!exists) {
                        Notification notification = notificationFactory.createBudgetExceeded(
                                user, month, generalCategory, budget.getAmount(), spent
                        );
                        notificationRepository.save(notification);
                    }
                });
    }
}
