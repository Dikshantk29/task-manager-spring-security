package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.entity.User;
import com.taskmanager.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
// ↑ marks this as a Spring-managed service bean
// Spring will automatically inject this wherever
// UserDetailsService interface is required

@RequiredArgsConstructor
// ↑ lombok generates constructor for all final fields
// replaces writing @Autowired manually

public class UserDetailsServiceImpl implements UserDetailsService {
// ↑ implementing Spring Security's UserDetailsService interface
// this is the contract Spring Security expects
// it has only ONE method to implement: loadUserByUsername()

    private final UserRepository userRepository;
    // ↑ ONLY dependency here — just the repository
    // NO PasswordEncoder injection here
    // that would cause circular dependency with SecurityConfig
    // SecurityConfig defines PasswordEncoder bean
    // if we inject it here → SecurityConfig needs this class
    // this class needs SecurityConfig → 💥 deadlock

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        // ─── STEP 1: FIND USER IN DATABASE ───────────────────────────
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + username
                ));
        // ↑ Optional.orElseThrow() safely handles "user not found"
        // if user doesn't exist → throws UsernameNotFoundException
        // Spring Security catches this and returns 401 automatically
        // you don't need to handle it manually

        // ─── STEP 2: TRANSLATE TO SPRING SECURITY'S UserDetails ──────
        return org.springframework.security.core.userdetails.User
                // ↑ this is Spring Security's built-in User class
                // NOT your entity User class
                // full package path used to avoid naming conflict
                .builder()
                .username(user.getUsername())
                // ↑ the username Spring Security will use for auth checks

                .password(user.getPassword())
                // ↑ already BCrypt hashed from registration
                // Spring Security will use PasswordEncoder.matches()
                // to compare this with what user typed at login

                .authorities(List.of(
                        new SimpleGrantedAuthority(user.getRole())
                ))
                // ↑ converts "ROLE_USER" string into a GrantedAuthority object
                // Spring Security uses this for:
                // .hasRole("USER") checks in SecurityConfig
                // @PreAuthorize("hasRole('USER')") checks in controllers

                .build();
        // ↑ constructs the final UserDetails object
        // Spring Security uses this object for the rest of the request
    }
}