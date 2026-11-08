package com.Project.GestionBanque.repositories;

import com.Project.GestionBanque.enteties.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationRepository extends JpaRepository<Operation, Long> {
  Page<Operation> findByCompteIdOrderByDateOperationDescIdDesc(Long id, Pageable pageable);
}
