package com.yourorg.banking.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class SecurityHeadersConfig {

    @Value("${app.security.security-headers.content-security-policy}")
    private String contentSecurityPolicy;

    @Value("${app.security.security-headers.x-frame-options}")
    private String xFrameOptions;

    @Value("${app.security.security-headers.x-content-type-options}")
    private String xContentTypeOptions;

    @Value("${app.security.security-headers.x-xss-protection}")
    private String xXssProtection;

    @Value("${app.security.security-headers.strict-transport-security}")
    private String strictTransportSecurity;

    @Value("${app.security.security-headers.referrer-policy}")
    private String referrerPolicy;

    @Value("${app.security.security-headers.permissions-policy}")
    private String permissionsPolicy;

    @Bean
    public SecurityHeadersFilter securityHeadersFilter() {
        return new SecurityHeadersFilter();
    }

    public class SecurityHeadersFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                      FilterChain filterChain) throws ServletException, IOException {
            
            // Set security headers
            setSecurityHeaders(response);
            
            // Continue with the filter chain
            filterChain.doFilter(request, response);
        }

        private void setSecurityHeaders(HttpServletResponse response) {
            // Content Security Policy
            if (contentSecurityPolicy != null && !contentSecurityPolicy.isEmpty()) {
                response.setHeader("Content-Security-Policy", contentSecurityPolicy);
            }

            // X-Frame-Options
            if (xFrameOptions != null && !xFrameOptions.isEmpty()) {
                response.setHeader("X-Frame-Options", xFrameOptions);
            }

            // X-Content-Type-Options
            if (xContentTypeOptions != null && !xContentTypeOptions.isEmpty()) {
                response.setHeader("X-Content-Type-Options", xContentTypeOptions);
            }

            // X-XSS-Protection
            if (xXssProtection != null && !xXssProtection.isEmpty()) {
                response.setHeader("X-XSS-Protection", xXssProtection);
            }

            // Strict-Transport-Security (only for HTTPS)
            if (strictTransportSecurity != null && !strictTransportSecurity.isEmpty()) {
                response.setHeader("Strict-Transport-Security", strictTransportSecurity);
            }

            // Referrer-Policy
            if (referrerPolicy != null && !referrerPolicy.isEmpty()) {
                response.setHeader("Referrer-Policy", referrerPolicy);
            }

            // Permissions-Policy
            if (permissionsPolicy != null && !permissionsPolicy.isEmpty()) {
                response.setHeader("Permissions-Policy", permissionsPolicy);
            }

            // Additional security headers
            response.setHeader("X-Permitted-Cross-Domain-Policies", "none");
            response.setHeader("Cross-Origin-Embedder-Policy", "require-corp");
            response.setHeader("Cross-Origin-Opener-Policy", "same-origin");
            response.setHeader("Cross-Origin-Resource-Policy", "same-origin");
            
            // Remove server information
            response.setHeader("Server", "");
            
            // Cache control for sensitive endpoints
            if (isSensitiveEndpoint(request)) {
                response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, proxy-revalidate");
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Expires", "0");
                response.setHeader("Surrogate-Control", "no-store");
            }
        }

        private boolean isSensitiveEndpoint(HttpServletRequest request) {
            String path = request.getRequestURI();
            return path.contains("/auth/") || 
                   path.contains("/login") || 
                   path.contains("/password") ||
                   path.contains("/admin/") ||
                   path.contains("/api/v1/accounts/") ||
                   path.contains("/api/v1/transfers/");
        }
    }

    /**
     * Get current security headers configuration
     */
    public Map<String, String> getSecurityHeadersConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("Content-Security-Policy", contentSecurityPolicy);
        config.put("X-Frame-Options", xFrameOptions);
        config.put("X-Content-Type-Options", xContentTypeOptions);
        config.put("X-XSS-Protection", xXssProtection);
        config.put("Strict-Transport-Security", strictTransportSecurity);
        config.put("Referrer-Policy", referrerPolicy);
        config.put("Permissions-Policy", permissionsPolicy);
        return config;
    }
}

