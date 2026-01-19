package com.swiftRoute.util;

import com.swiftRoute.entity.User;
import com.swiftRoute.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class JwtFilterChain extends OncePerRequestFilter {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String authorizationHeader = request.getHeader("Authorization");

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7); // Use substring instead of split
                String username = jwtUtil.getUsernameFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//                    User user = userRepository.findByEmail(username)
//                            .orElseThrow(() -> new RuntimeException("User not found"));

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

        } catch (io.jsonwebtoken.MalformedJwtException e) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid JWT token");

        } catch (io.jsonwebtoken.security.SignatureException e) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "JWT signature is invalid");

        } catch (Exception e) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication failed");
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
