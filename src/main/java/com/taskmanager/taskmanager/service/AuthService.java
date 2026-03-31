package com.taskmanager.taskmanager.service;
import com.taskmanager.taskmanager.dto.LoginRequest;
import com.taskmanager.taskmanager.dto.LoginResponse;
import com.taskmanager.taskmanager.dto.RegisterRequest;
import com.taskmanager.taskmanager.entity.User;
import com.taskmanager.taskmanager.repository.UserRepository;
import com.taskmanager.taskmanager.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    // ─── REGISTER ─────────────────────────────────────────────────────
    public String register(RegisterRequest request) {

        // 1. check if username already taken
        if (userRepository.existsByUsername(request.getUsername())) {
            return "Username already exists!";
        }

        // 2. build new User entity
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        //                               ↑
        //               NEVER save plain text password
        //               BCrypt encodes it before saving

        user.setRole(request.getRole());

        // 3. save to DB
        userRepository.save(user);

        return "User registered successfully!";
    }

    // ─── LOGIN ────────────────────────────────────────────────────────
    public LoginResponse login(LoginRequest request) {

        // 1. authenticate — this internally calls:
        //    UserDetailsServiceImpl.loadUserByUsername()
        //    BCryptPasswordEncoder.matches()
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),    // principal
                        request.getPassword()     // credentials
                )
        );
        //
        // If credentials are WRONG → authenticate() throws
        // AuthenticationException → Spring returns 401 automatically
        //
        // If credentials are RIGHT → returns Authentication object
        //

        // 2. fetch user from DB to get the role
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

        // 3. generate JWT token with username + role
        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getRole()
        );

        // 4. return token + user info
        return new LoginResponse(token, user.getUsername(), user.getRole());
    }
}