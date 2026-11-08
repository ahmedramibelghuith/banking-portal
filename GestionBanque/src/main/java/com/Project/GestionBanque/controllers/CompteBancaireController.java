package com.Project.GestionBanque.controllers;

import com.Project.GestionBanque.controllers.ApiModels.*;
import com.Project.GestionBanque.services.BankingService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comptes")
public class CompteBancaireController {
  private final BankingService bank;

  public CompteBancaireController(BankingService bank) {
    this.bank = bank;
  }

  @GetMapping
  public PageView<AccountView> accounts(
      Principal p,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "25") int size) {
    return bank.accounts(p.getName(), page, size);
  }

  @GetMapping("/summary")
  public AccountTotals totals(Principal principal) {
    return bank.accountTotals(principal.getName());
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
  public PageView<OperationView> history(
      Principal p,
      @PathVariable Long id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "25") int size) {
    return bank.history(p.getName(), id, page, size);
  }
}
