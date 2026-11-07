package com.Project.GestionBanque.services;

import com.Project.GestionBanque.controllers.ApiModels.*;
import com.Project.GestionBanque.enteties.*;
import com.Project.GestionBanque.repositories.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class BankingService {
  private final ClientRepository clients;
  private final CompteBancaireRepository accounts;
  private final OperationRepository operations;

  public BankingService(ClientRepository c, CompteBancaireRepository a, OperationRepository o) {
    clients = c;
    accounts = a;
    operations = o;
  }

  public Client user(String email) {
    return clients
        .findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
  }

  private CompteBancaire locked(Long id) {
    return accounts
        .findLocked(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
  }

  private void owns(CompteBancaire account, String email) {
    if (!account.getClient().getEmail().equals(email))
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "This account belongs to another customer");
  }

  private void active(CompteBancaire account) {
    if (!account.getEtat().equals("ACTIVE"))
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Account is closed");
  }

  private void amount(BigDecimal amount) {
    if (amount == null
        || amount.signum() <= 0
        || amount.scale() > 2
        || amount.compareTo(new BigDecimal("999999999999.99")) > 0)
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Enter a positive amount with at most two decimal places");
  }

  private void credit(CompteBancaire a, BigDecimal amount) {
    BigDecimal result = a.getSolde().add(amount);
    if (result.compareTo(new BigDecimal("99999999999999999.99")) > 0)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Balance limit exceeded");
    a.setSolde(result);
  }

  private void debit(CompteBancaire a, BigDecimal amount) {
    if (a.getSolde().compareTo(amount) < 0)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Insufficient funds");
    a.setSolde(a.getSolde().subtract(amount));
  }

  private OperationView record(
      CompteBancaire account, BigDecimal amount, TypeOperation type, String reference) {
    Operation operation = new Operation();
    operation.setCompte(account);
    operation.setMontant(amount);
    operation.setType(type);
    operation.setReference(reference);
    return OperationView.of(operations.save(operation));
  }

  @Transactional(readOnly = true)
  public List<AccountView> accounts(String email) {
    return accounts.findByClientIdOrderById(user(email).getId()).stream()
        .map(AccountView::of)
        .toList();
  }

  public AccountView create(String email, String type) {
    CompteBancaire account = "SAVINGS".equals(type) ? new CompteEpargne() : new CompteCourant();
    account.setClient(user(email));
    return AccountView.of(accounts.save(account));
  }

  public void close(String email, Long id) {
    CompteBancaire account = locked(id);
    owns(account, email);
    active(account);
    if (account.getSolde().signum() != 0)
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Empty the account before closing it");
    account.setEtat("FERME");
  }

  public OperationView deposit(String email, MoneyRequest r) {
    amount(r.montant());
    CompteBancaire account = locked(r.compteId());
    owns(account, email);
    active(account);
    credit(account, r.montant());
    return record(account, r.montant(), TypeOperation.CREDIT, UUID.randomUUID().toString());
  }

  public OperationView withdraw(String email, MoneyRequest r) {
    amount(r.montant());
    CompteBancaire account = locked(r.compteId());
    owns(account, email);
    active(account);
    debit(account, r.montant());
    return record(account, r.montant(), TypeOperation.DEBIT, UUID.randomUUID().toString());
  }

  public void transfer(String email, TransferRequest r) {
    amount(r.montant());
    if (r.compteSourceId().equals(r.compteDestinataireId()))
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Choose a different destination account");
    // Lock both accounts in ID order to avoid opposite transfers deadlocking.
    CompteBancaire first = locked(Math.min(r.compteSourceId(), r.compteDestinataireId()));
    CompteBancaire second = locked(Math.max(r.compteSourceId(), r.compteDestinataireId()));
    CompteBancaire source = first.getId().equals(r.compteSourceId()) ? first : second;
    CompteBancaire target = first == source ? second : first;
    owns(source, email);
    active(source);
    active(target);
    debit(source, r.montant());
    credit(target, r.montant());
    String reference = UUID.randomUUID().toString();
    record(source, r.montant(), TypeOperation.DEBIT, reference);
    record(target, r.montant(), TypeOperation.CREDIT, reference);
  }

  @Transactional(readOnly = true)
  public List<OperationView> history(String email, Long id) {
    CompteBancaire account =
        accounts
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    owns(account, email);
    return operations.findByCompteIdOrderByDateOperationDescIdDesc(id).stream()
        .map(OperationView::of)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<AccountView> allAccounts() {
    return accounts.findAll().stream().map(AccountView::of).toList();
  }

  @Transactional(readOnly = true)
  public List<OperationView> allOperations() {
    return operations.findAll().stream().map(OperationView::of).toList();
  }
}
