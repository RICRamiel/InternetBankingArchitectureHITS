package org.ricramiel.userservice.infrastructure.repositories;

import lombok.NonNull;
import org.ricramiel.userservice.domain.models.entities.OAuth2Provider;
import org.ricramiel.userservice.domain.models.entities.ids.OAuth2ProviderId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OAuth2ProviderRepository extends JpaRepository<OAuth2Provider, OAuth2ProviderId> {
}