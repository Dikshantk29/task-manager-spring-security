package com.taskmanager.taskmanager.config;

import com.taskmanager.taskmanager.security.JwtFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
// ↑ tells Spring: this class contains bean definitions
// Spring reads all @Bean methods inside and registers them

@EnableWebSecurity
// ↑ activates Spring Security for the whole application
// without this → Spring Security does nothing

@EnableMethodSecurity
// ↑ activates @PreAuthorize on controller methods
// without this → @PreAuthorize("hasRole('ADMIN')") won't work

@RequiredArgsConstructor
// ↑ lombok generates constructor for all final fields below
public class SecurityConfig {

    private final JwtFilter jwtFilter;
    // ↑ our custom filter that validates JWT on every request
    // registered in the filter chain below

    private final UserDetailsService userDetailsService;
    // ↑ injecting the INTERFACE — not UserDetailsServiceImpl directly
    // Spring sees UserDetailsServiceImpl implements this interface
    // and injects it automatically
    //
    // WHY interface and not implementation?
    // Using the implementation directly caused circular dependency:
    // SecurityConfig → needs UserDetailsServiceImpl
    // UserDetailsServiceImpl → needs PasswordEncoder
    // PasswordEncoder is defined in SecurityConfig
    // → each waiting for other → 💥 app fails to start
    //
    // Using the interface breaks that cycle ✅

    // ─── BEAN 1: SECURITY FILTER CHAIN ───────────────────────────────
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // ↑ disable CSRF protection
                // CSRF is for browser-based apps with sessions
                // our REST API uses JWT → no sessions → CSRF not needed

                // ADD THIS ↓
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                )

                .authorizeHttpRequests(auth -> auth

                                .requestMatchers("/auth/**").permitAll()
                                // ↑ /auth/register and /auth/login are fully public
                                // no token needed — these are entry points

                                .requestMatchers("/tasks/all").hasRole("ADMIN")
                                // ↑ ONLY users with ROLE_ADMIN can access this
                                // anyone else → 403 Forbidden automatically

                                .anyRequest().authenticated()
                        // ↑ every other endpoint requires a valid token
                        // no token or invalid token → 401 Unauthorized
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                // ↑ tells Spring: DO NOT create HTTP sessions
                // every request must carry its own JWT token
                // server remembers nothing between requests
                // makes the app fully stateless and scalable

                .authenticationProvider(authenticationProvider())
                // ↑ registers our custom DaoAuthenticationProvider
                // tells Spring: use THIS to verify credentials at login

                .addFilterBefore(jwtFilter,
                        UsernamePasswordAuthenticationFilter.class);
        // ↑ run OUR JwtFilter BEFORE Spring's default login filter
        // so that JWT is validated first on every request
        // if JwtFilter sets auth in SecurityContext →
        // Spring's default filter sees it and skips itself

        return http.build();
    }

    // ─── BEAN 2: PASSWORD ENCODER ────────────────────────────────────
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
        // ↑ BCrypt is the industry standard for password hashing
        // converts "pass123" → "$2a$10$randomhash..."
        // one-way hash — cannot be reversed
        // even same password hashes differently each time
        // used in two places:
        // 1. AuthService.register() → encode before saving
        // 2. DaoAuthenticationProvider → verify during login
    }

    // ─── BEAN 3: AUTHENTICATION PROVIDER ─────────────────────────────
    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        // ↑ THE FIX — pass userDetailsService directly in constructor
        // Spring Boot 3.x removed the no-arg constructor
        // new version REQUIRES UserDetailsService in constructor itself
        // OLD way → new DaoAuthenticationProvider()  ❌ doesn't exist anymore
        // NEW way → new DaoAuthenticationProvider(userDetailsService) ✅

        provider.setPasswordEncoder(passwordEncoder());
        // ↑ still set PasswordEncoder separately — this hasn't changed

        return provider;
    }

    // ─── BEAN 4: AUTHENTICATION MANAGER ──────────────────────────────
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
        // ↑ AuthenticationManager is the main entry point for login
        // AuthService calls this:
        // authenticationManager.authenticate(username, password)
        //           ↓
        // delegates to DaoAuthenticationProvider (bean 3)
        //           ↓
        // calls UserDetailsService (loads user from DB)
        //           ↓
        // calls PasswordEncoder (verifies password)
        //           ↓
        // success → returns authenticated user
        // failure → throws AuthenticationException → 401
    }
}
