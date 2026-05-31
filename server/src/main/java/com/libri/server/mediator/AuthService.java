package com.libri.server.mediator;

import com.libri.server.dto.LoginRequest;
import com.libri.server.dto.LoginResponse;
import com.libri.server.dto.RegisterRequest;
import com.libri.server.entity.User;
import com.libri.server.entity.UserRole;
import com.libri.server.exception.BusinessException;
import com.libri.server.foundation.UserRepository;
import com.libri.server.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public LoginResponse login(LoginRequest request) {
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Пользователь не найден"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("Неверный пароль");
        }

        String token = tokenProvider.generateToken(user.getId(), user.getRole().name());
        return new LoginResponse(token, user.getId(), user.getRole().name(),
                user.getFirstName(), user.getLastName(), user.getEmail());
    }

    public LoginResponse register(RegisterRequest request) {
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email уже зарегистрирован");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(UserRole.READER)
                .build();

        user = userRepo.save(user);
        String token = tokenProvider.generateToken(user.getId(), user.getRole().name());
        return new LoginResponse(token, user.getId(), user.getRole().name(),
                user.getFirstName(), user.getLastName(), user.getEmail());
    }
}
