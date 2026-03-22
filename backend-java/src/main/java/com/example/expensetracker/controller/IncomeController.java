package com.example.expensetracker.controller;

import com.example.expensetracker.model.Income;
import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.IncomeRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/income")
public class IncomeController {

    private final IncomeRepository incomeRepository;

    public IncomeController(IncomeRepository incomeRepository) {
        this.incomeRepository = incomeRepository;
    }

    public record IncomeRequest(
            String icon,
            @NotNull String source,
            @NotNull Double amount,
            @NotNull String date
    ) {}

    @PostMapping("/add")
    public ResponseEntity<?> addIncome(@AuthenticationPrincipal User user,
                                       @Valid @RequestBody IncomeRequest body) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("messege", "Unauthorized"));
        }
        LocalDate d = LocalDate.parse(body.date());
        Income income = Income.builder()
                .user(user)
                .icon(body.icon())
                .source(body.source())
                .amount(body.amount())
                .date(d)
                .build();
        incomeRepository.save(income);
        return ResponseEntity.ok(income);
    }

    @GetMapping("/get")
    public ResponseEntity<?> getAll(@AuthenticationPrincipal User user,
                                    @RequestParam(value = "page", defaultValue = "0") int page,
                                    @RequestParam(value = "size", defaultValue = "5") int size,
                                    @RequestParam(value = "from", required = false) String from,
                                    @RequestParam(value = "to", required = false) String to,
                                    @RequestParam(value = "source", required = false) String source) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("messege", "Unauthorized"));
        }
        LocalDate fromDate = (from == null || from.isBlank()) ? null : LocalDate.parse(from);
        LocalDate toDate = (to == null || to.isBlank()) ? null : LocalDate.parse(to);
        String sourceFilter = (source == null || source.isBlank()) ? null : source.trim();

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
        Page<Income> p = incomeRepository.findPage(user, fromDate, toDate, sourceFilter, pageable);
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
        incomeRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("messege", " Доход успешно удален "));
    }

    @GetMapping("/downloadexcel")
    public ResponseEntity<byte[]> downloadExcel(@AuthenticationPrincipal User user) throws IOException {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        List<Income> list = incomeRepository.findByUserOrderByDateDesc(user);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Доход");
        int rowIdx = 0;
        Row header = sheet.createRow(rowIdx++);
        header.createCell(0).setCellValue("Источник");
        header.createCell(1).setCellValue("Сумма");
        header.createCell(2).setCellValue("Дата");

        for (Income income : list) {
            Row row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(income.getSource());
            row.createCell(1).setCellValue(income.getAmount());
            row.createCell(2).setCellValue(income.getDate().toString());
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=income_details.xlsx");
        return ResponseEntity.ok()
                .headers(headers)
                .body(out.toByteArray());
    }
}
