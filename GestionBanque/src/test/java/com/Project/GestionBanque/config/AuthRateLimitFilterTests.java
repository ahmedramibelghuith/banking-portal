package com.Project.GestionBanque.config;

import static org.assertj.core.api.Assertions.*;

import java.time.*;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;

class AuthRateLimitFilterTests {
  @Test
  void blocksAtLimitResetsAfterWindowAndDoesNotTrustForwardedAddress() throws Exception {
    var time = new AtomicLong(0);
    Clock clock =
        new Clock() {
          public ZoneId getZone() {
            return ZoneOffset.UTC;
          }

          public Clock withZone(ZoneId zone) {
            return this;
          }

          public Instant instant() {
            return Instant.ofEpochMilli(time.get());
          }
        };
    var filter = new AuthRateLimitFilter(2, clock);
    for (int index = 0; index < 3; index++) {
      var request = new MockHttpServletRequest("POST", "/api/auth/login");
      request.setRemoteAddr("127.0.0.1");
      request.addHeader("X-Forwarded-For", "10.0.0." + index);
      var response = new MockHttpServletResponse();
      filter.doFilter(request, response, (req, res) -> {});
      assertThat(response.getStatus()).isEqualTo(index < 2 ? 200 : 429);
      if (index == 2) assertThat(response.getHeader("Retry-After")).isEqualTo("60");
    }
    time.set(60000);
    var response = new MockHttpServletResponse();
    var request = new MockHttpServletRequest("POST", "/api/auth/login");
    request.setRemoteAddr("127.0.0.1");
    filter.doFilter(request, response, (req, res) -> {});
    assertThat(response.getStatus()).isEqualTo(200);
  }
}
