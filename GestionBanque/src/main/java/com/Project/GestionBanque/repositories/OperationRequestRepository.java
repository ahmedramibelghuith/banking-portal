package com.Project.GestionBanque.repositories;

import com.Project.GestionBanque.enteties.OperationRequest;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationRequestRepository extends JpaRepository<OperationRequest, Long> {
  Optional<OperationRequest> findByClientIdAndRequestKey(Long clientId, String requestKey);
}
