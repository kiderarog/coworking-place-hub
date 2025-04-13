package io.neif.coworkingplacehub.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;
    private final ClientDetailsService clientDetailsService;

    @Autowired
    public JWTFilter(JWTUtil jwtUtil, ClientDetailsService clientDetailsService) {
        this.jwtUtil = jwtUtil;
        this.clientDetailsService = clientDetailsService;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException, ServletException {

        String requestURI = request.getRequestURI();
        System.out.println("Запрос URI: " + requestURI);

        if (requestURI.startsWith("/auth")) {
            System.out.println("Пропускаем запрос без проверки JWT для: " + requestURI);
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization Header: " + authHeader);

        if (authHeader != null && !authHeader.isBlank() && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);

            if (jwt.isBlank()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Невалидный токен.");
                return;
            }

            try {
                Map<String, String> claims = jwtUtil.validateTokenAndRetrieveClaims(jwt);
                String username = claims.get("username");
                System.out.println("Токен успешно проверен. Username: " + username);

                UserDetails userDetails = clientDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (JWTVerificationException e) {
                System.err.println("Ошибка валидации токена: " + e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Невалидный токен.");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

}
