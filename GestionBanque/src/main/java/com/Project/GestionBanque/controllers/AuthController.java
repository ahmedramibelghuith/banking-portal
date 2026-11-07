package com.Project.GestionBanque.controllers;

import com.Project.GestionBanque.controllers.ApiModels.*;
import com.Project.GestionBanque.enteties.Client;
import com.Project.GestionBanque.repositories.ClientRepository;
import com.Project.GestionBanque.services.BankingService;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final ClientRepository clients;
  private final PasswordEncoder passwords;
  private final BankingService bank;

  public AuthController(ClientRepository c, PasswordEncoder p, BankingService b) {
    clients = c;
    passwords = p;
    bank = b;
  }

  @GetMapping("/csrf")
  public Map<String, String> csrf(CsrfToken token) {
    return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
  }

  @GetMapping("/me")
  public UserView me(Principal principal) {
    return UserView.of(bank.user(principal.getName()));
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public UserView register(@Valid @RequestBody Registration r) {
    String email = r.email().trim().toLowerCase(Locale.ROOT);
    if (r.password().getBytes(StandardCharsets.UTF_8).length > 72)
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password exceeds 72 UTF-8 bytes");
    if (clients.findByEmail(email).isPresent())
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "An account with this email already exists");
    Client user = new Client();
    user.setEmail(email);
    user.setNom(r.nom().trim());
    user.setPrenom(r.prenom().trim());
    user.setPasswordHash(passwords.encode(r.password()));
    return UserView.of(clients.save(user));
  }
}
