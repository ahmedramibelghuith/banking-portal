package com.Project.GestionBanque.services;

import com.Project.GestionBanque.controllers.ApiModels.*;
import com.Project.GestionBanque.enteties.*;
import com.Project.GestionBanque.repositories.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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

  private final OperationRequestRepository requests;

  public BankingService(
      ClientRepository clients,
      CompteBancaireRepository accounts,
      OperationRepository operations,
      OperationRequestRepository requests) {
    this.clients = clients;
    this.accounts = accounts;
    this.operations = operations;
    this.requests = requests;
  }

  private Client lockCustomer(String email) {
    return clients
        .findLockedByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
  }

  private String fingerprint(String kind, Long source, Long target, BigDecimal amount) {
    return kind
        + ":"
        + source
        + ":"
        + target
        + ":"
        + amount.setScale(2, RoundingMode.UNNECESSARY).toPlainString();
  }

  private OperationRequest previous(Client client, String key, String fingerprint) {
    if (key == null
        || !key.matches(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"))
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Supply a UUID Idempotency-Key header");
    var previous =
        requests
            .findByClientIdAndRequestKey(client.getId(), key.toLowerCase(Locale.ROOT))
            .orElse(null);
    if (previous != null && !previous.getFingerprint().equals(fingerprint))
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "This Idempotency-Key was already used for a different request");
    return previous;
  }

  private void remember(Client client, String key, String fingerprint, Long operationId) {
    var request = new OperationRequest();
    request.setClient(client);
    request.setRequestKey(key.toLowerCase(Locale.ROOT));
    request.setFingerprint(fingerprint);
    if (operationId != null) request.setOperation(operations.getReferenceById(operationId));
    requests.save(request);
  }

  private static PageRequest page(int page, int size) {
    if (page < 0 || size < 1 || size > 100)
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Page must be nonnegative and size between 1 and 100");
    return PageRequest.of(page, size, Sort.by("id").descending());
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
    operation.setMontant(amount.setScale(2, RoundingMode.UNNECESSARY));
    operation.setType(type);
    operation.setReference(reference);
    return OperationView.of(operations.save(operation));
  }

  @Transactional(readOnly = true)
  public PageView<AccountView> accounts(String email, int page, int size) {
    return PageView.of(
        accounts.findByClientId(user(email).getId(), page(page, size)).map(AccountView::of));
  }

  @Transactional(readOnly = true)
  public AccountTotals accountTotals(String email) {
    Long id = user(email).getId();
    return new AccountTotals(accounts.activeTotal(id), accounts.countByClientId(id));
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

  public OperationView deposit(String email, MoneyRequest request, String key) {
    return cashOperation(email, request, key, TypeOperation.CREDIT);
  }

  public OperationView withdraw(String email, MoneyRequest request, String key) {
    return cashOperation(email, request, key, TypeOperation.DEBIT);
  }

  private OperationView cashOperation(
      String email, MoneyRequest request, String key, TypeOperation type) {
    amount(request.montant());
    // Serialize requests per customer before taking account locks, including duplicate retries.
    Client customer = lockCustomer(email);
    String fingerprint = fingerprint(type.name(), request.compteId(), null, request.montant());
    var previous = previous(customer, key, fingerprint);
    if (previous != null) return OperationView.of(previous.getOperation());
    var account = locked(request.compteId());
    owns(account, email);
    active(account);
    if (type == TypeOperation.CREDIT) credit(account, request.montant());
    else debit(account, request.montant());
    var result = record(account, request.montant(), type, UUID.randomUUID().toString());
    remember(customer, key, fingerprint, result.id());
    return result;
  }

  public void transfer(String email, TransferRequest request, String key) {
    amount(request.montant());
    Client customer = lockCustomer(email);
    String fingerprint =
        fingerprint(
            "TRANSFER",
            request.compteSourceId(),
            request.compteDestinataireId(),
            request.montant());
    if (previous(customer, key, fingerprint) != null) return;
    if (request.compteSourceId().equals(request.compteDestinataireId()))
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Choose a different destination account");
    // Lock both accounts in ID order to avoid opposite transfers deadlocking.
    var first = locked(Math.min(request.compteSourceId(), request.compteDestinataireId()));
    var second = locked(Math.max(request.compteSourceId(), request.compteDestinataireId()));
    var source = first.getId().equals(request.compteSourceId()) ? first : second;
    var target = first == source ? second : first;
    owns(source, email);
    active(source);
    active(target);
    debit(source, request.montant());
    credit(target, request.montant());
    String reference = UUID.randomUUID().toString();
    record(source, request.montant(), TypeOperation.DEBIT, reference);
    record(target, request.montant(), TypeOperation.CREDIT, reference);
    remember(customer, key, fingerprint, null);
  }

  @Transactional(readOnly = true)
  public PageView<OperationView> history(String email, Long id, int page, int size) {
    CompteBancaire account =
        accounts
            .findById(id)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    owns(account, email);
    return PageView.of(
        operations
            .findByCompteIdOrderByDateOperationDescIdDesc(id, page(page, size))
            .map(OperationView::of));
  }

  @Transactional(readOnly = true)
  public PageView<UserView> allClients(int page, int size) {
    return PageView.of(clients.findAll(page(page, size)).map(UserView::of));
  }

  @Transactional(readOnly = true)
  public PageView<AccountView> allAccounts(int page, int size) {
    return PageView.of(accounts.findAll(page(page, size)).map(AccountView::of));
  }

  @Transactional(readOnly = true)
  public PageView<OperationView> allOperations(int page, int size) {
    return PageView.of(operations.findAll(page(page, size)).map(OperationView::of));
  }
}
