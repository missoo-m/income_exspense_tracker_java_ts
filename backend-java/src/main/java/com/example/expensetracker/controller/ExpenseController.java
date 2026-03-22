package com.example.expensetracker.controller;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.model.Notification;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.BudgetRepository;
import com.example.expensetracker.repository.ExpenseRepository;
import com.example.expensetracker.repository.NotificationRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/expense")
public class ExpenseController {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final NotificationRepository notificationRepository;

    public ExpenseController(ExpenseRepository expenseRepository,
                             BudgetRepository budgetRepository,
                             NotificationRepository notificationRepository) {
        this.expenseRepository = expenseRepository;
        this.budgetRepository = budgetRepository;
        this.notificationRepository = notificationRepository;
    }

    public record ExpenseRequest(
            String icon,
            String category,
            @NotNull String generalCategory,
            String description,
            @NotNull Double amount,
            @NotNull String date
    ) {}

    @PostMapping("/add")
    public ResponseEntity<?> addExpense(@AuthenticationPrincipal User user,
                                        @Valid @RequestBody ExpenseRequest body) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("messege", "Unauthorized"));
        }
        if (body.generalCategory() == null || body.generalCategory().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Требуется общая категория"));
        }
        String category = body.category();
        if (category == null || category.isBlank()) {
            category = body.generalCategory();
        }
        LocalDate d = LocalDate.parse(body.date());
        Expense expense = Expense.builder()
                .user(user)
                .icon(body.icon())
                .category(category)
                .generalCategory(body.generalCategory())
                .description(body.description())
                .amount(body.amount())
                .date(d)
                .build();
        expenseRepository.save(expense);

        // Budget check (month + general category)
        String month = YearMonth.from(d).toString(); // YYYY-MM
        budgetRepository.findByUserAndMonthAndGeneralCategory(user, month, body.generalCategory())
                .ifPresent(budget -> {
                    LocalDate from = YearMonth.parse(month).atDay(1);
                    LocalDate to = YearMonth.parse(month).atEndOfMonth();
                    Double spent = expenseRepository.sumByUserAndGeneralCategoryAndMonth(user, body.generalCategory(), from, to);
                    if (spent != null && spent > budget.getAmount()) {
                        String type = "BUDGET_EXCEEDED";
                        boolean already = notificationRepository.existsByUserAndTypeAndMonthAndGeneralCategory(
                                user, type, month, body.generalCategory()
                        );
                        if (!already) {
                            notificationRepository.save(Notification.builder()
                                    .user(user)
                                    .type(type)
                                    .month(month)
                                    .generalCategory(body.generalCategory())
                                    .message("Бюджет превышен на " + body.generalCategory() + " (" + month + "). Бюджет: "
                                            + budget.getAmount() + ", потраченно: " + spent)
                                    .build());
                        }
                    }
                });

        return ResponseEntity.ok(expense);
    }

    @GetMapping("/get")
    public ResponseEntity<?> getAll(@AuthenticationPrincipal User user,
                                    @RequestParam(value = "page", defaultValue = "0") int page,
                                    @RequestParam(value = "size", defaultValue = "5") int size,
                                    @RequestParam(value = "from", required = false) String from,
                                    @RequestParam(value = "to", required = false) String to,
                                    @RequestParam(value = "generalCategory", required = false) String generalCategory,
                                    @RequestParam(value = "category", required = false) String category) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("messege", "Unauthorized"));
        }
        LocalDate fromDate = (from == null || from.isBlank()) ? null : LocalDate.parse(from);
        LocalDate toDate = (to == null || to.isBlank()) ? null : LocalDate.parse(to);
        String gen = (generalCategory == null || generalCategory.isBlank()) ? null : generalCategory.trim();
        String cat = (category == null || category.isBlank()) ? null : category.trim();

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<Expense> p = expenseRepository.findPage(user, fromDate, toDate, gen, cat, pageable);
        return ResponseEntity.ok(Map.of(
                "items", p.getContent(),
                "page", p.getNumber(),
                "size", p.getSize(),
                "totalItems", p.getTotalElements(),
                "totalPages", p.getTotalPages()
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@AuthenticationPrincipal User user,
                                    @PathVariable("id") Long id) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("messege", "Unauthorized"));
        }
        expenseRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("messege", " Расходы успешно удалены "));
    }

    @GetMapping("/downloadexcel")
    public ResponseEntity<byte[]> downloadExcel(@AuthenticationPrincipal User user) throws IOException {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        List<Expense> list = expenseRepository.findByUserOrderByDateDesc(user);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Расходы");
        int rowIdx = 0;
        Row header = sheet.createRow(rowIdx++);
        header.createCell(0).setCellValue("Общая категория");
        header.createCell(1).setCellValue("Категория");
        header.createCell(2).setCellValue("Описание");
        header.createCell(3).setCellValue("Сумма");
        header.createCell(4).setCellValue("Дата");

        for (Expense expense : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(expense.getGeneralCategory() != null ? expense.getGeneralCategory() : "");
            row.createCell(1).setCellValue(expense.getCategory());
            row.createCell(2).setCellValue(expense.getDescription() != null ? expense.getDescription() : "");
            row.createCell(3).setCellValue(expense.getAmount());
            row.createCell(4).setCellValue(expense.getDate().toString());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=expense_details.xlsx");
        return ResponseEntity.ok()
                .headers(headers)
                .body(out.toByteArray());
    }
}

