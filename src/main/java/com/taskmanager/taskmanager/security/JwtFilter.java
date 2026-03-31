package com.taskmanager.taskmanager.security;

import com.taskmanager.taskmanager.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor                      // lombok → generates constructor for final fields
public class JwtFilter extends OncePerRequestFilter {
//                       ↑
// OncePerRequestFilter guarantees this filter runs
// EXACTLY once per request — not multiple times

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // ─── STEP A: READ THE AUTHORIZATION HEADER ───────────────────
        String authHeader = request.getHeader("Authorization");
        //
        // Every secured request must have this header:
        // Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.xxxxx
        //

        String token = null;
        String username = null;

        // ─── STEP B: CHECK IF HEADER EXISTS AND STARTS WITH "Bearer " ─
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);

            if (jwtUtil.validateToken(token)) {
                username = jwtUtil.extractUsername(token);
            }
        }

//// TEMPORARY DEBUG — add these 4 lines
//        System.out.println("=== JWT DEBUG ===");
//        System.out.println("AUTH HEADER → " + authHeader);
//        System.out.println("USERNAME    → " + username);
//        if (token != null) {
//            System.out.println("ROLE        → " + jwtUtil.extractRole(token));
//        }
//        System.out.println("=================");

        // ─── STEP D: SET AUTHENTICATION IN SECURITY CONTEXT ──────────
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            //                                ↑
            // check if user is NOT already authenticated
            // avoids processing the same request twice

            // load full user details from DB
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // extract role from token
            String role = jwtUtil.extractRole(token);

            // create authentication object with role as authority
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails,                                    // who
                    null,                                           // credentials (not needed)
                    List.of(new SimpleGrantedAuthority(role))       // what role
            );

            // attach request details to authentication
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // ─── STEP E: SAVE TO SECURITY CONTEXT ────────────────────
            SecurityContextHolder.getContext().setAuthentication(authToken);
            //
            // Now Spring Security knows:
            // WHO  → dikshant
            // ROLE → ROLE_USER
            // For the rest of this request's lifecycle
            //
        }

        // ─── STEP F: MOVE TO NEXT FILTER ─────────────────────────────
        filterChain.doFilter(request, response);
        //
        // ALWAYS call this — passes request to next filter
        // or to your controller if all filters pass
        //
    }
}