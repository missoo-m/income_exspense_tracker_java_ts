package com.example.expensetracker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

import java.time.Instant;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty("_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(nullable = false)
    private String type; // e.g. BUDGET_EXCEEDED

    @Column(nullable = false, columnDefinition = "text")
    private String message;

    @Column(nullable = false)
    @Default
    private boolean read = false;

    private String month; // YYYY-MM (optional)
    private String generalCategory; // optional

    @Column(nullable = false, updatable = false)
    @Default
    private Instant createdAt = Instant.now();
}

