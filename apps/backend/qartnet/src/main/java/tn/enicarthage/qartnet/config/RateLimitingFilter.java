package tn.enicarthage.qartnet.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    @Value("${app.rate-limit.auth-requests-per-minute:5}")
    private int maxRequestsPerMinute;

    private record RequestCounter(int count, Instant windowStart) {}

    private final Map<String, RequestCounter> counters = new ConcurrentHashMap<>();

    private static final String[] RATE_LIMITED_PATHS = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/forgot-password"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        if (!isRateLimited(path)) {
            chain.doFilter(request, response);
            return;
        }

        String key = getClientKey(request) + ":" + path;
        Instant now = Instant.now();

        RequestCounter current = counters.compute(key, (k, v) -> {
            if (v == null || now.isAfter(v.windowStart().plusSeconds(60))) {
                return new RequestCounter(1, now);
            }
            return new RequestCounter(v.count() + 1, v.windowStart());
        });

        if (current.count() > maxRequestsPerMinute) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Too many requests. Please try again later.\",\"timestamp\":\"" + now + "\"}");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isRateLimited(String path) {
        for (String p : RATE_LIMITED_PATHS) {
            if (path.startsWith(p)) return true;
        }
        return false;
    }

    private String getClientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
