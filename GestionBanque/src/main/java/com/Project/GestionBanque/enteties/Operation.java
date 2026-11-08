package com.Project.GestionBanque.enteties;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Operation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  private CompteBancaire compte;

  @Column(nullable = false)
  private Instant dateOperation = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MICROS);

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal montant;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TypeOperation type;

  @Column(nullable = false)
  private String reference;
}
