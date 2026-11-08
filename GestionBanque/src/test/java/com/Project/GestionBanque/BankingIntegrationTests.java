package com.Project.GestionBanque;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.Project.GestionBanque.controllers.ApiModels.*;
import com.Project.GestionBanque.enteties.*;
import com.Project.GestionBanque.repositories.*;
import com.Project.GestionBanque.services.BankingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.*;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest(
    properties = {
      "spring.datasource.url=${TEST_DB_URL:jdbc:h2:mem:bank-tests;DB_CLOSE_DELAY=-1}",
      "spring.datasource.username=${TEST_DB_USER:sa}",
      "spring.datasource.password=${TEST_DB_PASSWORD:}",
      "spring.datasource.driver-class-name=${TEST_DB_DRIVER:org.h2.Driver}",
      "banking.auth.max-requests-per-minute=1000",
      "banking.admin.email=",
      "banking.admin.password="
    })
@AutoConfigureMockMvc
class BankingIntegrationTests {
  @Autowired MockMvc mvc;
  @Autowired BankingService bank;
  @Autowired ClientRepository clients;
  @Autowired CompteBancaireRepository accounts;
  @Autowired OperationRepository operations;
  @Autowired OperationRequestRepository requests;
  @Autowired PasswordEncoder passwords;
  @Autowired ObjectMapper json;
  Client alice;
  Client bob;

  @BeforeEach
  void setup() {
    requests.deleteAll();
    operations.deleteAll();
    accounts.deleteAll();
    clients.deleteAll();
    alice = customer("alice@example.com");
    bob = customer("bob@example.com");
  }

  Client customer(String email) {
    Client c = new Client();
    c.setNom("Test");
    c.setPrenom("Customer");
    c.setEmail(email);
    c.setPasswordHash(passwords.encode("Valid-password-123"));
    return clients.save(c);
  }

  Long account(String email) {
    return bank.create(email, "CURRENT").id();
  }

  MoneyRequest money(Long id, String amount) {
    return new MoneyRequest(id, new BigDecimal(amount));
  }

  OperationView deposit(String email, MoneyRequest request) {
    return bank.deposit(email, request, UUID.randomUUID().toString());
  }

  OperationView withdraw(String email, MoneyRequest request) {
    return bank.withdraw(email, request, UUID.randomUUID().toString());
  }

  void transfer(String email, TransferRequest request) {
    bank.transfer(email, request, UUID.randomUUID().toString());
  }

  List<OperationView> history(String email, Long id) {
    return bank.history(email, id, 0, 100).content();
  }

  @Test
  void registrationAndPasswordAuthentication() throws Exception {
    mvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType("application/json")
                .content(
                    "{\"nom\":\"New\",\"prenom\":\"User\",\"email\":\"new@example.com\",\"password\":\"Valid-password-123\",\"role\":\"ADMIN\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.role").value("USER"))
        .andExpect(jsonPath("$.passwordHash").doesNotExist());
    assertThat(clients.findByEmail("new@example.com").orElseThrow().getPasswordHash())
        .isNotEqualTo("Valid-password-123");
    mvc.perform(formLogin("/api/auth/login").user("new@example.com").password("wrong"))
        .andExpect(status().isUnauthorized());
    var login =
        mvc.perform(
                formLogin("/api/auth/login").user("new@example.com").password("Valid-password-123"))
            .andExpect(status().isNoContent())
            .andReturn();
    var session = (MockHttpSession) login.getRequest().getSession(false);
    mvc.perform(get("/api/auth/me").session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("new@example.com"));
    mvc.perform(
            post("/api/auth/logout")
                .session(session)
                .with(csrf())
                .header("Idempotency-Key", UUID.randomUUID().toString()))
        .andExpect(status().isNoContent());
    assertThat(session.isInvalid()).isTrue();
    // Reusing the destroyed session ID must not recover the authenticated context.
    mvc.perform(
            get("/api/auth/me")
                .cookie(new jakarta.servlet.http.Cookie("JSESSIONID", session.getId())))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void realCsrfTokenIsRequiredAndWorksForLogin() throws Exception {
    var response = mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk()).andReturn();
    var session = (MockHttpSession) response.getRequest().getSession(false);
    var token = json.readTree(response.getResponse().getContentAsString());
    mvc.perform(
            post("/api/auth/login")
                .session(session)
                .param("username", alice.getEmail())
                .param("password", "Valid-password-123"))
        .andExpect(status().isForbidden());
    mvc.perform(
            post("/api/auth/login")
                .session(session)
                .header(token.get("headerName").asText(), token.get("token").asText())
                .param("username", alice.getEmail())
                .param("password", "Valid-password-123"))
        .andExpect(status().isNoContent());
  }

  @Test
  void rejectsDuplicateAndInvalidRegistration() throws Exception {
    mvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType("application/json")
                .content(
                    "{\"nom\":\"Test\",\"prenom\":\"User\",\"email\":\"ALICE@example.com\",\"password\":\"Valid-password-123\"}"))
        .andExpect(status().isConflict());
    mvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType("application/json")
                .content(
                    "{\"nom\":\"Test\",\"prenom\":\"User\",\"email\":\"bad-email\",\"password\":\"short\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void authenticationRolesAndOwnershipAreEnforced() throws Exception {
    Long id = account(alice.getEmail());
    mvc.perform(get("/api/comptes")).andExpect(status().isUnauthorized());
    mvc.perform(get("/api/admin/clients").with(user(alice.getEmail())))
        .andExpect(status().isForbidden());
    mvc.perform(get("/api/admin/summary").with(user("admin@example.com").roles("ADMIN")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accounts").value(1));
    mvc.perform(get("/api/comptes").with(user(bob.getEmail())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isEmpty());
    mvc.perform(get("/api/comptes/" + id + "/operations").with(user(bob.getEmail())))
        .andExpect(status().isForbidden());
    mvc.perform(
            post("/api/operations/versement")
                .with(user(bob.getEmail()))
                .with(csrf())
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType("application/json")
                .content(json.writeValueAsString(money(id, "10.00"))))
        .andExpect(status().isForbidden());
    mvc.perform(
            delete("/api/comptes/" + id)
                .with(user(bob.getEmail()))
                .with(csrf())
                .header("Idempotency-Key", UUID.randomUUID().toString()))
        .andExpect(status().isForbidden());
  }

  @Test
  void rejectsNonPositiveAndOverprecisionAmounts() throws Exception {
    Long id = account(alice.getEmail());
    for (String amount : new String[] {"-10", "0", "1.001", "1000000000000.00"}) {
      mvc.perform(
              post("/api/operations/retrait")
                  .with(user(alice.getEmail()))
                  .with(csrf())
                  .header("Idempotency-Key", UUID.randomUUID().toString())
                  .contentType("application/json")
                  .content(json.writeValueAsString(money(id, amount))))
          .andExpect(status().isBadRequest());
    }
    assertThat(accounts.findById(id).orElseThrow().getSolde()).isEqualByComparingTo("0");
  }

  @Test
  void decimalDepositsAndWithdrawalsRetainExactBalances() {
    Long id = account(alice.getEmail());
    deposit(alice.getEmail(), money(id, "0.10"));
    deposit(alice.getEmail(), money(id, "0.20"));
    withdraw(alice.getEmail(), money(id, "0.10"));
    assertThat(accounts.findById(id).orElseThrow().getSolde()).isEqualByComparingTo("0.20");
    assertThat(operations.count()).isEqualTo(3);
  }

  @Test
  void transferIsAtomicAndRetainsLinkedLedgerEntries() {
    Long source = account(alice.getEmail()), target = account(bob.getEmail());
    deposit(alice.getEmail(), money(source, "100.00"));
    transfer(alice.getEmail(), new TransferRequest(source, target, new BigDecimal("25.25")));
    assertThat(accounts.findById(source).orElseThrow().getSolde()).isEqualByComparingTo("74.75");
    assertThat(accounts.findById(target).orElseThrow().getSolde()).isEqualByComparingTo("25.25");
    var sourceHistory = history(alice.getEmail(), source);
    var targetHistory = history(bob.getEmail(), target);
    assertThat(sourceHistory.getFirst().reference())
        .isEqualTo(targetHistory.getFirst().reference());
    long before = operations.count();
    assertThatThrownBy(
            () ->
                transfer(
                    alice.getEmail(), new TransferRequest(source, target, new BigDecimal("1000"))))
        .isInstanceOf(ResponseStatusException.class);
    assertThat(operations.count()).isEqualTo(before);
    assertThat(accounts.findById(target).orElseThrow().getSolde()).isEqualByComparingTo("25.25");
    assertThatThrownBy(
            () -> transfer(alice.getEmail(), new TransferRequest(source, source, BigDecimal.ONE)))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void emptyAccountClosurePreservesHistoryAndRejectsFurtherTransactions() {
    Long id = account(alice.getEmail());
    deposit(alice.getEmail(), money(id, "10"));
    assertThatThrownBy(() -> bank.close(alice.getEmail(), id))
        .isInstanceOf(ResponseStatusException.class);
    withdraw(alice.getEmail(), money(id, "10"));
    bank.close(alice.getEmail(), id);
    assertThat(history(alice.getEmail(), id)).hasSize(2);
    assertThat(accounts.findById(id).orElseThrow().getEtat()).isEqualTo("FERME");
    assertThatThrownBy(() -> deposit(alice.getEmail(), money(id, "1")))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void concurrentWithdrawalsCannotOverspend() throws Exception {
    Long id = account(alice.getEmail());
    deposit(alice.getEmail(), money(id, "150"));
    var barrier = new CyclicBarrier(2);
    try (var pool = Executors.newFixedThreadPool(2)) {
      Callable<Boolean> withdraw =
          () -> {
            barrier.await(10, TimeUnit.SECONDS);
            try {
              withdraw(alice.getEmail(), money(id, "100"));
              return true;
            } catch (ResponseStatusException ex) {
              if (ex.getStatusCode().value() != 409) throw ex;
              return false;
            }
          };
      var first = pool.submit(withdraw);
      var second = pool.submit(withdraw);
      assertThat(
              (first.get(15, TimeUnit.SECONDS) ? 1 : 0)
                  + (second.get(15, TimeUnit.SECONDS) ? 1 : 0))
          .isEqualTo(1);
    }
    assertThat(accounts.findById(id).orElseThrow().getSolde()).isEqualByComparingTo("50");
    assertThat(operations.count()).isEqualTo(2);
  }

  @Test
  void failureAfterDebitRollsBackBothAccountsAndLedger() {
    Long source = account(alice.getEmail()), target = account(bob.getEmail());
    deposit(alice.getEmail(), money(source, "100.00"));
    var receiver = accounts.findById(target).orElseThrow();
    receiver.setSolde(new BigDecimal("99999999999999999.99"));
    accounts.saveAndFlush(receiver);
    long ledgerBefore = operations.count(), requestsBefore = requests.count();
    assertThatThrownBy(
            () -> transfer(alice.getEmail(), new TransferRequest(source, target, BigDecimal.ONE)))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("Balance limit exceeded");
    assertThat(accounts.findById(source).orElseThrow().getSolde()).isEqualByComparingTo("100.00");
    assertThat(accounts.findById(target).orElseThrow().getSolde())
        .isEqualByComparingTo("99999999999999999.99");
    assertThat(operations.count()).isEqualTo(ledgerBefore);
    assertThat(requests.count()).isEqualTo(requestsBefore);
  }

  @Test
  void duplicateRequestsReplayOriginalResultAndRejectChangedPayload() {
    Long id = account(alice.getEmail());
    String key = UUID.randomUUID().toString();
    var original = bank.deposit(alice.getEmail(), money(id, "10.00"), key);
    var replay = bank.deposit(alice.getEmail(), money(id, "10.0"), key);
    assertThat(replay).isEqualTo(original);
    assertThat(operations.count()).isEqualTo(1);
    assertThat(accounts.findById(id).orElseThrow().getSolde()).isEqualByComparingTo("10.00");
    assertThatThrownBy(() -> bank.deposit(alice.getEmail(), money(id, "11"), key))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("different request");
  }

  @Test
  void concurrentDuplicateTransfersApplyOnceAndKeysAreCustomerScoped() throws Exception {
    Long source = account(alice.getEmail()), target = account(bob.getEmail());
    deposit(alice.getEmail(), money(source, "100"));
    String key = UUID.randomUUID().toString();
    var barrier = new CyclicBarrier(2);
    try (var pool = Executors.newFixedThreadPool(2)) {
      Callable<Void> transfer =
          () -> {
            barrier.await(10, TimeUnit.SECONDS);
            bank.transfer(
                alice.getEmail(), new TransferRequest(source, target, new BigDecimal("25")), key);
            return null;
          };
      var first = pool.submit(transfer);
      var second = pool.submit(transfer);
      first.get(20, TimeUnit.SECONDS);
      second.get(20, TimeUnit.SECONDS);
    }
    assertThat(accounts.findById(source).orElseThrow().getSolde()).isEqualByComparingTo("75");
    assertThat(accounts.findById(target).orElseThrow().getSolde()).isEqualByComparingTo("25");
    assertThat(operations.count()).isEqualTo(3);
    bank.deposit(bob.getEmail(), money(target, "1"), key);
    assertThat(accounts.findById(target).orElseThrow().getSolde()).isEqualByComparingTo("26");
  }

  @Test
  void moneyAndIdentifiersAreSerializedWithoutPrecisionLoss() throws Exception {
    Long id = account(alice.getEmail());
    deposit(alice.getEmail(), money(id, "0.10"));
    var account = accounts.findById(id).orElseThrow();
    account.setSolde(new BigDecimal("99999999999999999.99"));
    accounts.saveAndFlush(account);
    mvc.perform(get("/api/comptes").with(user(alice.getEmail())))
        .andExpect(jsonPath("$.content[0].id").isString())
        .andExpect(jsonPath("$.content[0].id").value(id.toString()))
        .andExpect(jsonPath("$.content[0].clientId").value(alice.getId().toString()))
        .andExpect(jsonPath("$.content[0].solde").isString())
        .andExpect(jsonPath("$.content[0].solde").value("99999999999999999.99"));
    mvc.perform(get("/api/comptes/summary").with(user(alice.getEmail())))
        .andExpect(jsonPath("$.total").value("99999999999999999.99"));
  }

  @Test
  void pagesAreBoundedAndStable() throws Exception {
    Long id = account(alice.getEmail());
    for (int index = 0; index < 4; index++) deposit(alice.getEmail(), money(id, "1"));
    mvc.perform(
            get("/api/comptes/" + id + "/operations?page=0&size=2").with(user(alice.getEmail())))
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.totalElements").value(4))
        .andExpect(jsonPath("$.last").value(false));
    mvc.perform(
            get("/api/comptes/" + id + "/operations?page=1&size=2").with(user(alice.getEmail())))
        .andExpect(jsonPath("$.content.length()").value(2))
        .andExpect(jsonPath("$.last").value(true));
    mvc.perform(get("/api/admin/clients?size=101").with(user("admin@example.com").roles("ADMIN")))
        .andExpect(status().isBadRequest());
    mvc.perform(get("/api/comptes?page=-1").with(user(alice.getEmail())))
        .andExpect(status().isBadRequest());
  }

  @Test
  void unicodePasswordByteLimitIsEnforced() throws Exception {
    var registration = new Registration("Last", "First", "unicode@example.com", "界".repeat(25));
    mvc.perform(
            post("/api/auth/register")
                .with(csrf())
                .contentType("application/json")
                .content(json.writeValueAsString(registration)))
        .andExpect(status().isBadRequest());
    assertThat(clients.findByEmail("unicode@example.com")).isEmpty();
  }

  @Test
  void flywayMigrationsHaveRun() {
    assertThat(flyway.info().applied()).hasSize(2);
    assertThat(flyway.validateWithResult().validationSuccessful).isTrue();
  }

  @Test
  void committedMoneyIsVisibleOutsideThePersistenceContext() throws Exception {
    Long id = account(alice.getEmail());
    deposit(alice.getEmail(), money(id, "123.45"));
    try (var connection = dataSource.getConnection();
        var query = connection.prepareStatement("SELECT solde FROM compte_bancaire WHERE id = ?")) {
      query.setLong(1, id);
      try (var result = query.executeQuery()) {
        assertThat(result.next()).isTrue();
        assertThat(result.getBigDecimal(1)).isEqualByComparingTo("123.45");
      }
    }
  }

  @Autowired javax.sql.DataSource dataSource;
  @Autowired org.flywaydb.core.Flyway flyway;

  @Test
  void oppositeTransfersCompleteWithoutLostUpdates() throws Exception {
    Long source = account(alice.getEmail()), target = account(bob.getEmail());
    deposit(alice.getEmail(), money(source, "100"));
    deposit(bob.getEmail(), money(target, "100"));
    var barrier = new CyclicBarrier(2);
    try (var pool = Executors.newFixedThreadPool(2)) {
      var first =
          pool.submit(
              () -> {
                barrier.await(10, TimeUnit.SECONDS);
                transfer(
                    alice.getEmail(), new TransferRequest(source, target, new BigDecimal("25")));
                return null;
              });
      var second =
          pool.submit(
              () -> {
                barrier.await(10, TimeUnit.SECONDS);
                transfer(bob.getEmail(), new TransferRequest(target, source, new BigDecimal("10")));
                return null;
              });
      first.get(20, TimeUnit.SECONDS);
      second.get(20, TimeUnit.SECONDS);
    }
    assertThat(accounts.findById(source).orElseThrow().getSolde()).isEqualByComparingTo("85");
    assertThat(accounts.findById(target).orElseThrow().getSolde()).isEqualByComparingTo("115");
    assertThat(operations.count()).isEqualTo(6);
  }

  @Test
  void fractionalIdentifiersCannotBeSilentlyTruncated() throws Exception {
    Long source = account(alice.getEmail());
    mvc.perform(
            post("/api/operations/virement")
                .with(user(alice.getEmail()))
                .with(csrf())
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType("application/json")
                .content(
                    "{\"compteSourceId\":\""
                        + source
                        + "\",\"compteDestinataireId\":1.5,\"montant\":\"1.00\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void largeDestinationIdentifiersStayExactAtTheHttpBoundary() throws Exception {
    Long source = account(alice.getEmail());
    long destination = 9007199254740993L;
    deposit(alice.getEmail(), money(source, "10"));
    try (var connection = dataSource.getConnection();
        var insert =
            connection.prepareStatement(
                "INSERT INTO compte_bancaire(id,date_creation,solde,etat,client_id,version) VALUES"
                    + " (?,CURRENT_DATE,0,'ACTIVE',?,0)")) {
      insert.setLong(1, destination);
      insert.setLong(2, bob.getId());
      insert.executeUpdate();
      try (var child = connection.prepareStatement("INSERT INTO compte_courant(id) VALUES (?)")) {
        child.setLong(1, destination);
        child.executeUpdate();
      }
    }
    mvc.perform(
            post("/api/operations/virement")
                .with(user(alice.getEmail()))
                .with(csrf())
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .contentType("application/json")
                .content(
                    "{\"compteSourceId\":\""
                        + source
                        + "\",\"compteDestinataireId\":\"9007199254740993\",\"montant\":\"1.00\"}"))
        .andExpect(status().isOk());
    assertThat(accounts.findById(destination).orElseThrow().getSolde())
        .isEqualByComparingTo("1.00");
  }
}
