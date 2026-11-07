package com.Project.GestionBanque.enteties;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Inheritance(strategy = InheritanceType.JOINED)
public class CompteBancaire {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private LocalDate dateCreation = LocalDate.now();

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal solde = BigDecimal.ZERO;

  @Column(nullable = false)
  private String etat = "ACTIVE";

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private Client client;

  @Version private long version;

  public String getType() {
    return this instanceof CompteEpargne ? "SAVINGS" : "CURRENT";
  }
}
