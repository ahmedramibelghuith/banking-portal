package com.Project.GestionBanque.controllers;

import com.Project.GestionBanque.enteties.*;
import com.Project.GestionBanque.validation.Utf8Bytes;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

public final class ApiModels {
  private ApiModels() {}

  public record Registration(
      @NotBlank @Size(max = 100) String nom,
      @NotBlank @Size(max = 100) String prenom,
      @NotBlank @Email @Size(max = 254) String email,
      @NotBlank @Size(min = 12, max = 64) @Utf8Bytes(max = 72) String password) {}

  public record UserView(
      @JsonSerialize(using = ToStringSerializer.class) Long id,
      String nom,
      String prenom,
      String email,
      String role) {
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
      @JsonSerialize(using = ToStringSerializer.class) Long id,
      LocalDate dateCreation,
      @JsonSerialize(using = ToStringSerializer.class) BigDecimal solde,
      String etat,
      String type,
      @JsonSerialize(using = ToStringSerializer.class) Long clientId,
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
      @JsonSerialize(using = ToStringSerializer.class) Long id,
      @JsonSerialize(using = ToStringSerializer.class) Long compteId,
      Instant dateOperation,
      @JsonSerialize(using = ToStringSerializer.class) BigDecimal montant,
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

  public record PageView<T>(
      java.util.List<T> content, int page, int size, long totalElements, boolean last) {
    public static <T> PageView<T> of(org.springframework.data.domain.Page<T> page) {
      return new PageView<>(
          page.getContent(),
          page.getNumber(),
          page.getSize(),
          page.getTotalElements(),
          page.isLast());
    }
  }

  public record AccountTotals(
      @JsonSerialize(using = ToStringSerializer.class) BigDecimal total, long count) {}

  public record Summary(long clients, long accounts, long transactions) {}
}
