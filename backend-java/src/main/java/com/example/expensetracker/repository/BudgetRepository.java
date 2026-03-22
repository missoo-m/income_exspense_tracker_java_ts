package com.example.expensetracker.repository;

import com.example.expensetracker.model.Budget;
import com.example.expensetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUserAndMonthOrderByGeneralCategoryAsc(User user, String month);
    Optional<Budget> findByUserAndMonthAndGeneralCategory(User user, String month, String generalCategory);
    Optional<Budget> findByIdAndUser(Long id, User user);
    void deleteByUser(User user);
}

