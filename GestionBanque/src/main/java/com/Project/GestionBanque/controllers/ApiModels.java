package com.Project.GestionBanque.controllers;

import com.Project.GestionBanque.enteties.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

public final class ApiModels {
  private ApiModels() {}

  public record Registration(
      @NotBlank @Size(max = 100) String nom,
      @NotBlank @Size(max = 100) String prenom,
      @NotBlank @Email @Size(max = 254) String email,
      @NotBlank @Size(min = 12, max = 64) String password) {}

  public record UserView(Long id, String nom, String prenom, String email, String role) {
    public static UserView of(Client c) {
      return new UserView(c.getId(), c.getNom(), c.getPrenom(), c.getEmail(), c.getRole());
    }
  }

  public record AccountRequest(@NotBlank @Pattern(regexp = "CURRENT|SAVINGS") String type) {}

  public record MoneyRequest(
      @NotNull @Positive Long compteId,
      @NotNull @DecimalMin("0.01") @Digits(integer = 12, fraction = 2) BigDecimal montant) {}

  public record TransferRequest(
      @NotNull @Positive Long compteSourceId,
      @NotNull @Positive Long compteDestinataireId,
      @NotNull @DecimalMin("0.01") @Digits(integer = 12, fraction = 2) BigDecimal montant) {}

  public record AccountView(
      Long id,
      LocalDate dateCreation,
      BigDecimal solde,
      String etat,
      String type,
      Long clientId,
      String clientEmail) {
    public static AccountView of(CompteBancaire c) {
      return new AccountView(
          c.getId(),
          c.getDateCreation(),
          c.getSolde(),
          c.getEtat(),
          c.getType(),
          c.getClient().getId(),
          c.getClient().getEmail());
    }
  }

  public record OperationView(
      Long id,
      Long compteId,
      Instant dateOperation,
      BigDecimal montant,
      TypeOperation type,
      String reference) {
    public static OperationView of(Operation o) {
      return new OperationView(
          o.getId(),
          o.getCompte().getId(),
          o.getDateOperation(),
          o.getMontant(),
          o.getType(),
          o.getReference());
    }
  }

  public record Summary(long clients, long accounts, long transactions) {}
}
