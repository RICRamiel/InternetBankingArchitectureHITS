package org.ricramiel.preferencesservice.repositories;

import org.ricramiel.preferencesservice.models.UserPreferences;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserPreferencesRepository extends JpaRepository<UserPreferences, UUID> {
}
