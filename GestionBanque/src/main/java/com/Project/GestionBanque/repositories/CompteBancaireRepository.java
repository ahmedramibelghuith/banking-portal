package com.Project.GestionBanque.repositories;

import com.Project.GestionBanque.enteties.CompteBancaire;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface CompteBancaireRepository extends JpaRepository<CompteBancaire, Long> {
  List<CompteBancaire> findByClientIdOrderById(Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select c from CompteBancaire c where c.id = :id")
  Optional<CompteBancaire> findLocked(@Param("id") Long id);
}
