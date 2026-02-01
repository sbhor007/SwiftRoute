package com.swiftRoute.util;

import com.swiftRoute.response.ApiResponse;
import com.swiftRoute.service.RedisService;
import com.swiftRoute.service.TokenService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Component
@Slf4j
@AllArgsConstructor
public class JwtFilterChain extends OncePerRequestFilter {
    private JwtUtil jwtUtil;
    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            log.info("JwtFilterChain: Processing request to {}", request.getRequestURI());
            String authorizationHeader = request.getHeader("Authorization");

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                log.info("JwtFilterChain: Found Bearer token");
                if(!tokenService.validateToken(authorizationHeader)){
                    log.warn("JWT filter chain : Token is blacklisted");
                    sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                            "JWT token has expired");
                    return;
                }
                String token = authorizationHeader.substring(7); // Use substring instead of split

                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);


                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    log.info("JwtFilterChain: Authenticating user {}", username);
                    Collection<SimpleGrantedAuthority> authorities =
                            List.of(new SimpleGrantedAuthority("ROLE_" + role));

                    // Use the User object directly, not just email
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "JWT token has expired");
            return;

        } catch (io.jsonwebtoken.MalformedJwtException e) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid JWT token");
            return;

        } catch (io.jsonwebtoken.security.SignatureException e) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "JWT signature is invalid");
            return;
        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication failed");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, int status, String message)
            throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("""
        {
          "success": false,
          "message": "%s"
        }
        """.formatted(message));
    }

}
