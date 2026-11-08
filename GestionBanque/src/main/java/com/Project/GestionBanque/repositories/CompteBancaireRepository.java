package com.Project.GestionBanque.repositories;

import com.Project.GestionBanque.enteties.CompteBancaire;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface CompteBancaireRepository extends JpaRepository<CompteBancaire, Long> {
  Page<CompteBancaire> findByClientId(Long id, Pageable pageable);

  long countByClientId(Long id);

  @Query(
      "select coalesce(sum(c.solde), 0) from CompteBancaire c where c.client.id = :id and c.etat ="
          + " 'ACTIVE'")
  BigDecimal activeTotal(@Param("id") Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select c from CompteBancaire c where c.id = :id")
  Optional<CompteBancaire> findLocked(@Param("id") Long id);
}
