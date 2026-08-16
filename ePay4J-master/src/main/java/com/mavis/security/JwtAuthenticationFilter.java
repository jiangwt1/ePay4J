package com.mavis.security;

import com.mavis.model.admin.dto.Result;
import com.mavis.common.constant.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.getUsername(token);
            String role = jwtTokenProvider.getRole(token);
            Long userId = jwtTokenProvider.getUserId(token);

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userId, null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        } else if (request.getRequestURI().startsWith("/api/admin/") &&
                !request.getRequestURI().equals("/api/admin/auth/login") &&
                !request.getRequestURI().equals("/api/admin/auth/reset-password")) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            objectMapper.writeValue(response.getOutputStream(), Result.fail(401, "未登录或登录已过期"));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader(Constants.TOKEN_HEADER);
        if (StringUtils.hasText(bearer) && bearer.startsWith(Constants.TOKEN_PREFIX)) {
            return bearer.substring(Constants.TOKEN_PREFIX.length());
        }
        return null;
    }
}
