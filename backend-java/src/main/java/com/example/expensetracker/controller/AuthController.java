package com.example.expensetracker.controller;

import com.example.expensetracker.model.User;
import com.example.expensetracker.security.JwtService;
import com.example.expensetracker.service.UserService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final Path uploadRoot;

    public AuthController(UserService userService,
                          JwtService jwtService,
                          PasswordEncoder passwordEncoder,
                          @Value("${app.upload.dir}") String uploadDir) throws IOException {
        this.userService = userService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadRoot);
    }

    public record RegisterRequest(
            @NotBlank String fullName,
            @Email String email,
            @NotBlank String password,
            String profileImageUrl,
            String role
    ) {}

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest body) {
        try {
            User user = userService.register(
                    body.fullName(),
                    body.email(),
                    body.password(),
                    body.role(),
                    body.profileImageUrl()
            );
            String token = jwtService.generateToken(user.getId());
            return ResponseEntity.created(URI.create("/api/v1/auth/getUser"))
                    .body(Map.of(
                            "id", user.getId(),
                            "user", user,
                            "token", token
                    ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("messege", e.getMessage()));
        }
    }

    public record LoginRequest(
            @Email String email,
            @NotBlank String password
    ) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest body) {
        var userOpt = userService.findByEmail(body.email());
        if (userOpt.isEmpty() || !passwordEncoder.matches(body.password(), userOpt.get().getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("messege", "Неверные учетные данные"));
        }
        User user = userOpt.get();
        String token = jwtService.generateToken(user.getId());
        return ResponseEntity.status(201).body(Map.of(
                "id", user.getId(),
                "user", user,
                "token", token
        ));
    }

    @GetMapping("/getUser")
    public ResponseEntity<?> getUser(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("messege", "Несанкционированный"));
        }
        return ResponseEntity.ok(currentUser);
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(value = "fullName", required = false) String fullName,
            @RequestParam(value = "password", required = false) String password,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }
        User user = userService.findById(currentUser.getId())
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));
        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName);
        }
        if (password != null && !password.isBlank()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        if (profileImage != null && !profileImage.isEmpty()) {
            String fileName = System.currentTimeMillis() + "-" + profileImage.getOriginalFilename();
            try {
                Path target = uploadRoot.resolve(fileName);
                profileImage.transferTo(target.toFile());
                String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                        .path("/uploads/")
                        .path(fileName)
                        .toUriString();
                user.setProfileImageUrl(imageUrl);
            } catch (IOException e) {
                return ResponseEntity.status(500).body(Map.of("message", "Ошибка загрузки файла"));
            }
        }
        User saved = userService.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "Профиль успешно обновлен",
                "user", saved
        ));
    }

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadImage(@RequestParam("image") MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Ни один файл не загружен"));
        }
        String fileName = System.currentTimeMillis() + "-" + image.getOriginalFilename();
        try {
            Path target = uploadRoot.resolve(fileName);
            image.transferTo(target.toFile());
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("message", "Ошибка загрузки файла"));
        }
        String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(fileName)
                .toUriString();
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }
}

