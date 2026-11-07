package com.Project.GestionBanque.enteties;

import jakarta.persistence.Entity;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class CompteEpargne extends CompteBancaire {
  private BigDecimal tauxInteret = new BigDecimal("2.00");
}
