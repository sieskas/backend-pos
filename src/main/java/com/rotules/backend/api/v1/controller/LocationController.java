package com.rotules.backend.api.v1.controller;

import com.rotules.backend.api.v1.controller.resources.LocationTreeDTO;
import com.rotules.backend.domain.Location;
import com.rotules.backend.domain.User;
import com.rotules.backend.services.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @Transactional
    @GetMapping("/tree")
    public ResponseEntity<List<LocationTreeDTO>> getLocationTree(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(Collections.emptyList());
    }

    private LocationTreeDTO convertToTreeDTO(Location location) {
        return new LocationTreeDTO(
                location.getId(),
                location.getLabel(),
                location.getChildren().stream()
                        .map(this::convertToTreeDTO)
                        .collect(Collectors.toList())
        );
    }
}