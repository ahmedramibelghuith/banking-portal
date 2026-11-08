package com.Project.GestionBanque.config;

import com.Project.GestionBanque.controllers.ApiModels.Registration;
import com.Project.GestionBanque.enteties.Client;
import com.Project.GestionBanque.repositories.ClientRepository;
import jakarta.validation.Validator;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminBootstrap {
  @Bean
  CommandLineRunner admin(
      ClientRepository clients,
      Validator validator,
      PasswordEncoder passwords,
      @Value("${banking.admin.email}") String email,
      @Value("${banking.admin.password}") String password) {
    return args -> {
      if (email.isBlank() && password.isBlank()) return;
      String normalized = email.trim().toLowerCase(Locale.ROOT);
      if (!validator
          .validate(new Registration("Administrator", "Portal", normalized, password))
          .isEmpty())
        throw new IllegalArgumentException(
            "Set a valid ADMIN_EMAIL and a 12–64 character ADMIN_PASSWORD of at most 72 UTF-8"
                + " bytes");
      var existing = clients.findByEmail(normalized);
      if (existing.isPresent()) {
        if (!existing.get().getRole().equals("ADMIN"))
          throw new IllegalStateException(
              "Admin email already belongs to a customer; choose another email");
        return;
      }
      Client admin = new Client();
      admin.setEmail(normalized);
      admin.setNom("Administrator");
      admin.setPrenom("Portal");
      admin.setRole("ADMIN");
      admin.setPasswordHash(passwords.encode(password));
      clients.save(admin);
    };
  }
}
