package com.Project.GestionBanque.controllers;

import com.Project.GestionBanque.controllers.ApiModels.*;
import com.Project.GestionBanque.repositories.*;
import com.Project.GestionBanque.services.BankingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
  private final BankingService bank;
  private final ClientRepository clients;
  private final CompteBancaireRepository accounts;
  private final OperationRepository operations;

  public AdminController(
      BankingService bank,
      ClientRepository clients,
      CompteBancaireRepository accounts,
      OperationRepository operations) {
    this.bank = bank;
    this.clients = clients;
    this.accounts = accounts;
    this.operations = operations;
  }

  @GetMapping("/summary")
  public Summary summary() {
    return new Summary(clients.count(), accounts.count(), operations.count());
  }

  @GetMapping("/clients")
  public PageView<UserView> clients(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
    return bank.allClients(page, size);
  }

  @GetMapping("/comptes")
  public PageView<AccountView> accounts(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
    return bank.allAccounts(page, size);
  }

  @GetMapping("/operations")
  public PageView<OperationView> operations(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "25") int size) {
    return bank.allOperations(page, size);
  }
}
