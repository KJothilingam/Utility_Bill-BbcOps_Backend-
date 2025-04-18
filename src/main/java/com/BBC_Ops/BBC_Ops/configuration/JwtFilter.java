package com.BBC_Ops.BBC_Ops.configuration;

import com.BBC_Ops.BBC_Ops.Model.ActiveToken;
import com.BBC_Ops.BBC_Ops.Model.Customer;
import com.BBC_Ops.BBC_Ops.Model.Employee;
import com.BBC_Ops.BBC_Ops.Repository.ActiveTokenRepository;
import com.BBC_Ops.BBC_Ops.Service.CustomerService;
import com.BBC_Ops.BBC_Ops.Service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired private JwtUtil jwtUtil;
    @Autowired private CustomerJwtUtil customerJwtUtil;

    @Autowired private EmployeeService employeeService;
    @Autowired private CustomerService customerService;

    @Autowired private ActiveTokenRepository activeTokenRepository;

    @Autowired private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or malformed Authorization header");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String origin = request.getHeader("Origin");

        try {
            if ("http://localhost:4200".equals(origin)) {
                // 🔐 EMPLOYEE
                Claims claims = jwtUtil.extractClaims(token);
                String email = claims.getSubject();

                validateActiveToken(email, token, response);

                Employee employee = employeeService.findByEmail(email);
                if (employee == null) {
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Employee not found");
                    return;
                }

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        employee, null, List.of(new SimpleGrantedAuthority(employee.getDesignation()))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);

            } else if ("http://localhost:5200".equals(origin)) {
                // 🔐 CUSTOMER
                Claims claims = customerJwtUtil.extractClaims(token);
                String email = claims.getSubject();

                validateActiveToken(email, token, response);

                Customer customer = customerService.findByEmail(email);
                if (customer == null) {
                    sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Customer not found");
                    return;
                }

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        customer, null, List.of(new SimpleGrantedAuthority("CUSTOMER"))
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                logger.warn("Unknown Origin: " + origin);
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unknown client origin");
                return;
            }

        } catch (ExpiredJwtException e) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
            return;
        } catch (Exception e) {
            logger.error("Error during token validation", e);
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token invalid");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void validateActiveToken(String email, String token, HttpServletResponse response) throws IOException {
        ActiveToken activeToken = activeTokenRepository.findByEmail(email);
        if (activeToken == null || !activeToken.getToken().equals(token)) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return; // ✅ This is required
        }
    }


    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
