package com.Project.GestionBanque.repositories;

import com.Project.GestionBanque.enteties.Client;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientRepository extends JpaRepository<Client, Long> {
  Optional<Client> findByEmail(String email);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select c from Client c where c.email = :email")
  Optional<Client> findLockedByEmail(@Param("email") String email);
}
