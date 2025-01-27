package com.rotules.backend.outcall.db.repository;

import com.rotules.backend.domain.Location;
import com.rotules.backend.domain.LocationTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LocationRepository  extends JpaRepository<Location, Long> {
    @Query("SELECT l FROM Location l WHERE l.id = (SELECT u.rootLocation.id FROM User u WHERE u.id = :userId)")
    Optional<Location> findRootLocationByUser(Long userId);

    @Query("SELECT l FROM Location l LEFT JOIN FETCH l.children WHERE l.id = :rootLocationId")
    Optional<Location> findByIdWithChildren(Long rootLocationId);
}
