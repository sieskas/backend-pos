package com.rotules.backend.services;

import com.rotules.backend.domain.Location;
import com.rotules.backend.domain.LocationType;
import com.rotules.backend.domain.LocationTypeEnum;
import com.rotules.backend.outcall.db.repository.LocationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    public Location createLocation(String label, LocationTypeEnum type, Location parent) {
        Location location = new Location();
        location.setLabel(label);
        location.setType(type);
        if (parent != null) {
            parent.addChild(location);
        }
        return locationRepository.save(location);
    }

    public List<Location> getRootLocations() {
        return locationRepository.findByParentIsNull();
    }

    public List<Location> getLocationsByType(LocationTypeEnum type) {
        return locationRepository.findByType_Name(type);
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