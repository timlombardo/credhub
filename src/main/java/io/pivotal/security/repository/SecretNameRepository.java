package io.pivotal.security.repository;

import io.pivotal.security.entity.SecretName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SecretNameRepository extends JpaRepository<SecretName, UUID> {
  SecretName findOneByNameIgnoreCase(String name);
}
