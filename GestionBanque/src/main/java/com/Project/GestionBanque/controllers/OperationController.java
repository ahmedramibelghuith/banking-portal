package com.Project.GestionBanque.controllers;

import com.Project.GestionBanque.controllers.ApiModels.*;
import com.Project.GestionBanque.services.BankingService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/operations")
public class OperationController {
  private final BankingService bank;

  public OperationController(BankingService bank) {
    this.bank = bank;
  }

  @PostMapping("/versement")
  public OperationView deposit(
      Principal p,
      @Valid @RequestBody MoneyRequest r,
      @RequestHeader("Idempotency-Key") String key) {
    return bank.deposit(p.getName(), r, key);
  }

  @PostMapping("/retrait")
  public OperationView withdraw(
      Principal p,
      @Valid @RequestBody MoneyRequest r,
      @RequestHeader("Idempotency-Key") String key) {
    return bank.withdraw(p.getName(), r, key);
  }

  @PostMapping("/virement")
  public void transfer(
      Principal p,
      @Valid @RequestBody TransferRequest r,
      @RequestHeader("Idempotency-Key") String key) {
    bank.transfer(p.getName(), r, key);
  }
}
