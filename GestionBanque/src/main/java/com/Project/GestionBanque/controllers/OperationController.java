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
  public OperationView deposit(Principal p, @Valid @RequestBody MoneyRequest r) {
    return bank.deposit(p.getName(), r);
  }

  @PostMapping("/retrait")
  public OperationView withdraw(Principal p, @Valid @RequestBody MoneyRequest r) {
    return bank.withdraw(p.getName(), r);
  }

  @PostMapping("/virement")
  public void transfer(Principal p, @Valid @RequestBody TransferRequest r) {
    bank.transfer(p.getName(), r);
  }
}
