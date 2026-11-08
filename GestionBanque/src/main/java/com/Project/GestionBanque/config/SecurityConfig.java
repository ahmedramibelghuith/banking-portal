package com.Project.GestionBanque.config;

import com.Project.GestionBanque.repositories.ClientRepository;
import java.util.Locale;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.savedrequest.NullRequestCache;

@Configuration
public class SecurityConfig {
  @Bean
  PasswordEncoder passwords() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  UserDetailsService users(ClientRepository clients) {
    return email ->
        clients
            .findByEmail(email.trim().toLowerCase(Locale.ROOT))
            .map(
                c ->
                    User.withUsername(c.getEmail())
                        .password(c.getPasswordHash())
                        .roles(c.getRole())
                        .build())
            .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
  }

  @Bean
  FilterRegistrationBean<AuthRateLimitFilter> limiterRegistration(AuthRateLimitFilter filter) {
    var registration = new FilterRegistrationBean<>(filter);
    registration.setEnabled(false);
    return registration;
  }

  @Bean
  SecurityFilterChain security(HttpSecurity http, AuthRateLimitFilter rateLimit) throws Exception {
    http.addFilterBefore(rateLimit, UsernamePasswordAuthenticationFilter.class)
        .authorizeHttpRequests(
            a ->
                a.requestMatchers(
                        "/api/auth/csrf", "/api/auth/register", "/api/auth/login", "/error")
                    .permitAll()
                    .requestMatchers("/api/admin/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/**")
                    .authenticated()
                    .anyRequest()
                    .permitAll())
        .requestCache(c -> c.requestCache(new NullRequestCache()))
        .formLogin(
            f ->
                f.loginProcessingUrl("/api/auth/login")
                    .successHandler((req, res, auth) -> res.setStatus(204))
                    .failureHandler(
                        (req, res, ex) -> {
                          res.setStatus(401);
                          res.setContentType("application/json");
                          res.getWriter().write("{\"message\":\"Invalid email or password\"}");
                        }))
        .logout(
            l ->
                l.logoutUrl("/api/auth/logout")
                    .logoutSuccessHandler((req, res, auth) -> res.setStatus(204)))
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint(
                        (req, res, ex) -> {
                          res.setStatus(401);
                          res.setContentType("application/json");
                          res.getWriter().write("{\"message\":\"Please sign in\"}");
                        })
                    .accessDeniedHandler(
                        (req, res, ex) -> {
                          res.setStatus(403);
                          res.setContentType("application/json");
                          res.getWriter()
                              .write(
                                  "{\"message\":\"Access denied or session expired. Sign in"
                                      + " again.\"}");
                        }));
    return http.build();
  }
}
