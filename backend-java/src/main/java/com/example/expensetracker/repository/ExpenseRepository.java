package com.example.expensetracker.repository;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUserOrderByDateDesc(User user);

    List<Expense> findByUserAndDateGreaterThanEqualOrderByDateDesc(User user, LocalDate from);

    @Query("select coalesce(sum(e.amount),0) from Expense e where e.user = :user")
    Double sumByUser(@Param("user") User user);

    @Query("""
            select coalesce(sum(e.amount),0)
            from Expense e
            where e.user = :user
              and e.generalCategory = :generalCategory
              and e.date >= :from
              and e.date <= :to
            """)
    Double sumByUserAndGeneralCategoryAndMonth(
            @Param("user") User user,
            @Param("generalCategory") String generalCategory,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
            select e
            from Expense e
            where e.user = :user
              and e.date >= coalesce(:from, e.date)
              and e.date <= coalesce(:to, e.date)
              and (coalesce(:generalCategory, '') = '' or e.generalCategory = :generalCategory)
              and (coalesce(:category, '') = '' or e.category = :category)
            order by e.date desc
            """)
    Page<Expense> findPage(
            @Param("user") User user,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("generalCategory") String generalCategory,
            @Param("category") String category,
            Pageable pageable
    );

    void deleteByUser(User user);
}

