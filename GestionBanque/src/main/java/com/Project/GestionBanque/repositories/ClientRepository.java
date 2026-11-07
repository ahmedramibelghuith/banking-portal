package com.Project.GestionBanque.repositories;

import com.Project.GestionBanque.enteties.Client;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
  Optional<Client> findByEmail(String email);
}
