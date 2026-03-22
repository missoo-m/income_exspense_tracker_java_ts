package com.example.expensetracker.security;

import com.example.expensetracker.model.User;
import com.example.expensetracker.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String clientUrl;

    public OAuth2SuccessHandler(
            JwtService jwtService,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.client-url}") String clientUrl
    ) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.clientUrl = clientUrl;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        if (!(authentication instanceof OAuth2AuthenticationToken token)) {
            response.sendError(500, "Неверный тип аутентификации");
            return;
        }

        OAuth2User oauthUser = token.getPrincipal();
        String registrationId = token.getAuthorizedClientRegistrationId(); // google, github

        String email = extractEmail(registrationId, oauthUser);
        String fullName = extractName(registrationId, oauthUser);
        String picture = extractPicture(registrationId, oauthUser);

        User user = upsertUser(email, fullName, picture);
        String jwt = jwtService.generateToken(user.getId());

        String redirectUrl = UriComponentsBuilder
                .fromUriString(clientUrl)
                .path("/oauth2/callback")
                .queryParam("token", jwt)
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

    private User upsertUser(String email, String fullName, String picture) {
        Optional<User> existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            User u = existing.get();
            if (fullName != null && !fullName.isBlank()) {
                u.setFullName(fullName);
            }
            if (picture != null && !picture.isBlank()) {
                u.setProfileImageUrl(picture);
            }
            return userRepository.save(u);
        }

        // Создаём пользователя без реального пароля (он не нужен для OAuth),
        // но поле password в БД обязательное.
        String randomPassword = UUID.randomUUID().toString();
        return userRepository.save(User.builder()
                .fullName((fullName == null || fullName.isBlank()) ? "OAuth User" : fullName)
                .email(email)
                .password(passwordEncoder.encode(randomPassword))
                .profileImageUrl(picture)
                .role(User.Role.NORMAL)
                .build());
    }

    private String extractEmail(String provider, OAuth2User oauthUser) {
        Object email = oauthUser.getAttribute("email");
        if (email != null && !email.toString().isBlank()) {
            return email.toString();
        }

        // GitHub часто не отдаёт email без доп. scope; fallback на login
        Object login = oauthUser.getAttribute("login");
        if ("github".equals(provider) && login != null) {
            return login + "@github.local";
        }

        // Последний fallback (не идеально, но не ломает флоу)
        return "user-" + UUID.randomUUID() + "@oauth.local";
    }

    private String extractName(String provider, OAuth2User oauthUser) {
        Object name = oauthUser.getAttribute("name");
        if (name != null && !name.toString().isBlank()) {
            return name.toString();
        }
        if ("github".equals(provider)) {
            Object login = oauthUser.getAttribute("login");
            if (login != null) return login.toString();
        }
        return "OAuth User";
    }

    private String extractPicture(String provider, OAuth2User oauthUser) {
        if ("google".equals(provider)) {
            Object pic = oauthUser.getAttribute("picture");
            return pic == null ? null : pic.toString();
        }
        if ("github".equals(provider)) {
            Object pic = oauthUser.getAttribute("avatar_url");
            return pic == null ? null : pic.toString();
        }
        return null;
    }
}

