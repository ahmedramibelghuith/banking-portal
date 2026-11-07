package com.Project.GestionBanque.controllers;

import com.Project.GestionBanque.controllers.ApiModels.*;
import com.Project.GestionBanque.repositories.*;
import com.Project.GestionBanque.services.BankingService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
  private final BankingService bank;
  private final ClientRepository clients;
  private final CompteBancaireRepository accounts;
  private final OperationRepository operations;

  public AdminController(
      BankingService b, ClientRepository c, CompteBancaireRepository a, OperationRepository o) {
    bank = b;
    clients = c;
    accounts = a;
    operations = o;
  }

  @GetMapping("/summary")
  public Summary summary() {
    return new Summary(clients.count(), accounts.count(), operations.count());
  }

  @GetMapping("/clients")
  public List<UserView> clients() {
    return clients.findAll().stream().map(UserView::of).toList();
  }

  @GetMapping("/comptes")
  public List<AccountView> accounts() {
    return bank.allAccounts();
  }

  @GetMapping("/operations")
  public List<OperationView> operations() {
    return bank.allOperations();
  }
}
