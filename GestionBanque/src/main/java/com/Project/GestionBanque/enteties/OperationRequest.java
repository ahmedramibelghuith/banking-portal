package com.Project.GestionBanque.enteties;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    name = "operation_request",
    uniqueConstraints = @UniqueConstraint(columnNames = {"client_id", "request_key"}))
public class OperationRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "client_id")
  private Client client;

  @Column(name = "request_key", nullable = false, length = 36)
  private String requestKey;

  @Column(nullable = false, length = 256)
  private String fingerprint;

  @ManyToOne(fetch = FetchType.LAZY)
  private Operation operation;
}
