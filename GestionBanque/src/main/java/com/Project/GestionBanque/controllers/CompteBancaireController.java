package com.Project.GestionBanque.controllers;

import com.Project.GestionBanque.controllers.ApiModels.*;
import com.Project.GestionBanque.services.BankingService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comptes")
public class CompteBancaireController {
  private final BankingService bank;

  public CompteBancaireController(BankingService bank) {
    this.bank = bank;
  }

  @GetMapping
  public List<AccountView> accounts(Principal p) {
    return bank.accounts(p.getName());
  }

  @PostMapping
  public AccountView create(Principal p, @Valid @RequestBody AccountRequest r) {
    return bank.create(p.getName(), r.type());
  }

  @DeleteMapping("/{id}")
  public void close(Principal p, @PathVariable Long id) {
    bank.close(p.getName(), id);
  }

  @GetMapping("/{id}/operations")
  public List<OperationView> history(Principal p, @PathVariable Long id) {
    return bank.history(p.getName(), id);
  }
}
