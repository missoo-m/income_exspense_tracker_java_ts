package com.example.expensetracker.repository;

import com.example.expensetracker.model.Income;
import com.example.expensetracker.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface IncomeRepository extends JpaRepository<Income, Long> {

    List<Income> findByUserOrderByDateDesc(User user);

    List<Income> findByUserAndDateGreaterThanEqualOrderByDateDesc(User user, LocalDate from);

    @Query("select coalesce(sum(i.amount),0) from Income i where i.user = :user")
    Double sumByUser(@Param("user") User user);

    @Query("""
            select i
            from Income i
            where i.user = :user
              and i.date >= coalesce(:from, i.date)
              and i.date <= coalesce(:to, i.date)
              and (coalesce(:source, '') = '' or lower(i.source) like lower(concat('%', :source, '%')))
            order by i.date desc
            """)
    Page<Income> findPage(
            @Param("user") User user,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("source") String source,
            Pageable pageable
    );

    void deleteByUser(User user);
}

