package com.Project.GestionBanque.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {
  private record Window(long starts, int count) {}

  private final Map<String, Window> windows = new HashMap<>();
  private final Clock clock;
  private final int limit;

  @org.springframework.beans.factory.annotation.Autowired
  public AuthRateLimitFilter(@Value("${banking.auth.max-requests-per-minute:20}") int limit) {
    this(limit, Clock.systemUTC());
  }

  AuthRateLimitFilter(int limit, Clock clock) {
    if (limit < 1) throw new IllegalArgumentException("Authentication limit must be positive");
    this.limit = limit;
    this.clock = clock;
  }

  private synchronized long retryAfter(String address) {
    long now = clock.millis();
    var window = windows.get(address);
    if (window == null || now - window.starts() >= 60000) {
      if (windows.size() >= 10000)
        windows.entrySet().removeIf(entry -> now - entry.getValue().starts() >= 60000);
      if (window == null && windows.size() >= 10000) return 60;
      windows.put(address, new Window(now, 1));
      return 0;
    }
    if (window.count() >= limit) return Math.max(1, (60000 - (now - window.starts()) + 999) / 1000);
    windows.put(address, new Window(window.starts(), window.count() + 1));
    return 0;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    return !request.getMethod().equals("POST")
        || !(request.getRequestURI().equals(request.getContextPath() + "/api/auth/login")
            || request.getRequestURI().equals(request.getContextPath() + "/api/auth/register"));
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    long retry = retryAfter(request.getRemoteAddr());
    if (retry > 0) {
      response.setStatus(429);
      response.setHeader("Retry-After", Long.toString(retry));
      response.setContentType("application/json");
      response
          .getWriter()
          .write("{\"message\":\"Too many sign-in attempts. Please wait and try again.\"}");
      return;
    }
    chain.doFilter(request, response);
  }
}
