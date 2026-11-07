package com.Project.GestionBanque.repositories;

import com.Project.GestionBanque.enteties.Operation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationRepository extends JpaRepository<Operation, Long> {
  List<Operation> findByCompteIdOrderByDateOperationDescIdDesc(Long id);
}
