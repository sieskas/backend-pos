package com.rotules.backend.services;

import com.rotules.backend.domain.Location;
import com.rotules.backend.domain.LocationTypeEnum;
import com.rotules.backend.domain.User;
import com.rotules.backend.outcall.db.repository.LocationRepository;
import com.rotules.backend.outcall.db.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Location createLocation(String label, LocationTypeEnum type, Long parentId) {
        var location = new Location();
        location.setLabel(label);
        location.setType(type);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var user = (authentication != null && authentication.getPrincipal() instanceof User u) ? u : null;

        Location locationSaved;

        if (parentId != null) {
            var parent = locationRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Parent location not found with id: " + parentId));

            location.setParent(parent);
            parent.getChildren().add(location);
            locationSaved = locationRepository.save(location);
        } else {
            locationSaved = locationRepository.save(location);
            if (user != null) {
                var attachedUser = userRepository.findById(user.getId()).orElseThrow(() -> new EntityNotFoundException("User not found"));
                if (attachedUser.getRootLocation() != null) {
                    throw new DataIntegrityViolationException("L'utilisateur possède déjà une location racine.");
                }

                attachedUser.setRootLocation(locationSaved);
                userRepository.save(attachedUser);
            } else {
                throw new AccessDeniedException("User is not logged in");
            }
        }
        return locationSaved;
    }

    @Transactional
    public Location findRootLocationByUser(User user) {
        return locationRepository.findRootLocationByUser(user.getId())
                .map(this::loadChildren) // Charge les enfants
                .orElseThrow(() -> new RuntimeException("Aucune location racine trouvée pour cet utilisateur."));
    }

    private Location loadChildren(Location rootLocation) {
        rootLocation.getChildren().size(); // Force le chargement des enfants
        return rootLocation;
    }


    public Optional<Location> getLocationById(Long id) {
        return locationRepository.findById(id);
    }

    public List<Location> getLocationHierarchy(Long locationId) {
        return getLocationById(locationId)
                .map(Location::getPath)
                .orElse(Collections.emptyList());
    }
}