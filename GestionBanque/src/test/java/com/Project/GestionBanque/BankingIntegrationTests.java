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
      "spring.datasource.url=jdbc:h2:mem:bank-tests;DB_CLOSE_DELAY=-1",
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
  @Autowired PasswordEncoder passwords;
  @Autowired ObjectMapper json;
  Client alice;
  Client bob;

  @BeforeEach
  void setup() {
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

  @Test
  void registrationAndPasswordAuthentication() throws Exception {
    mvc.perform(
            post("/api/auth/register")
                .with(csrf())
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
    mvc.perform(post("/api/auth/logout").session(session).with(csrf()))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
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
                .contentType("application/json")
                .content(
                    "{\"nom\":\"Test\",\"prenom\":\"User\",\"email\":\"ALICE@example.com\",\"password\":\"Valid-password-123\"}"))
        .andExpect(status().isConflict());
    mvc.perform(
            post("/api/auth/register")
                .with(csrf())
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
        .andExpect(content().json("[]"));
    mvc.perform(get("/api/comptes/" + id + "/operations").with(user(bob.getEmail())))
        .andExpect(status().isForbidden());
    mvc.perform(
            post("/api/operations/versement")
                .with(user(bob.getEmail()))
                .with(csrf())
                .contentType("application/json")
                .content(json.writeValueAsString(money(id, "10.00"))))
        .andExpect(status().isForbidden());
    mvc.perform(delete("/api/comptes/" + id).with(user(bob.getEmail())).with(csrf()))
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
                  .contentType("application/json")
                  .content(json.writeValueAsString(money(id, amount))))
          .andExpect(status().isBadRequest());
    }
    assertThat(accounts.findById(id).orElseThrow().getSolde()).isEqualByComparingTo("0");
  }

  @Test
  void decimalDepositsAndWithdrawalsRetainExactBalances() {
    Long id = account(alice.getEmail());
    bank.deposit(alice.getEmail(), money(id, "0.10"));
    bank.deposit(alice.getEmail(), money(id, "0.20"));
    bank.withdraw(alice.getEmail(), money(id, "0.10"));
    assertThat(accounts.findById(id).orElseThrow().getSolde()).isEqualByComparingTo("0.20");
    assertThat(operations.count()).isEqualTo(3);
  }

  @Test
  void transferIsAtomicAndRetainsLinkedLedgerEntries() {
    Long source = account(alice.getEmail()), target = account(bob.getEmail());
    bank.deposit(alice.getEmail(), money(source, "100.00"));
    bank.transfer(alice.getEmail(), new TransferRequest(source, target, new BigDecimal("25.25")));
    assertThat(accounts.findById(source).orElseThrow().getSolde()).isEqualByComparingTo("74.75");
    assertThat(accounts.findById(target).orElseThrow().getSolde()).isEqualByComparingTo("25.25");
    var sourceHistory = bank.history(alice.getEmail(), source);
    var targetHistory = bank.history(bob.getEmail(), target);
    assertThat(sourceHistory.getFirst().reference())
        .isEqualTo(targetHistory.getFirst().reference());
    long before = operations.count();
    assertThatThrownBy(
            () ->
                bank.transfer(
                    alice.getEmail(), new TransferRequest(source, target, new BigDecimal("1000"))))
        .isInstanceOf(ResponseStatusException.class);
    assertThat(operations.count()).isEqualTo(before);
    assertThat(accounts.findById(target).orElseThrow().getSolde()).isEqualByComparingTo("25.25");
    assertThatThrownBy(
            () ->
                bank.transfer(
                    alice.getEmail(), new TransferRequest(source, source, BigDecimal.ONE)))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void emptyAccountClosurePreservesHistoryAndRejectsFurtherTransactions() {
    Long id = account(alice.getEmail());
    bank.deposit(alice.getEmail(), money(id, "10"));
    assertThatThrownBy(() -> bank.close(alice.getEmail(), id))
        .isInstanceOf(ResponseStatusException.class);
    bank.withdraw(alice.getEmail(), money(id, "10"));
    bank.close(alice.getEmail(), id);
    assertThat(bank.history(alice.getEmail(), id)).hasSize(2);
    assertThat(accounts.findById(id).orElseThrow().getEtat()).isEqualTo("FERME");
    assertThatThrownBy(() -> bank.deposit(alice.getEmail(), money(id, "1")))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void concurrentWithdrawalsCannotOverspend() throws Exception {
    Long id = account(alice.getEmail());
    bank.deposit(alice.getEmail(), money(id, "150"));
    var barrier = new CyclicBarrier(2);
    try (var pool = Executors.newFixedThreadPool(2)) {
      Callable<Boolean> withdraw =
          () -> {
            barrier.await(10, TimeUnit.SECONDS);
            try {
              bank.withdraw(alice.getEmail(), money(id, "100"));
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
}
