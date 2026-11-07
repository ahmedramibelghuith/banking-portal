package com.Project.GestionBanque.enteties;

import jakarta.persistence.Entity;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class CompteCourant extends CompteBancaire {
  private BigDecimal decouvert = BigDecimal.ZERO;
}
