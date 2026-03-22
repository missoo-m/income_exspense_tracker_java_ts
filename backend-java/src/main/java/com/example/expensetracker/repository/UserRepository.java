package com.example.expensetracker.repository;

import com.example.expensetracker.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByRole(User.Role role);

    @Query("""
            select u from User u
            where (:from is null or u.createdAt >= :from)
              and (:to is null or u.createdAt < :to)
            order by u.createdAt desc
            """)
    Page<User> findPage(@Param("from") Instant from, @Param("to") Instant to, Pageable pageable);

    @Query("""
            select u from User u
            where u.createdAt >= :from
              and u.createdAt < :to
            order by u.createdAt desc
            """)
    List<User> findAllBetween(@Param("from") Instant from, @Param("to") Instant to);
}

