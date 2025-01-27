package com.rotules.backend.api.v1.controller;

import com.rotules.backend.api.v1.controller.resources.LocationDetailsDTO;
import com.rotules.backend.api.v1.controller.resources.LocationStructureDTO;
import com.rotules.backend.api.v1.controller.resources.LocationTreeDTO;
import com.rotules.backend.domain.Location;
import com.rotules.backend.domain.LocationTypeEnum;
import com.rotules.backend.domain.User;
import com.rotules.backend.services.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @Transactional
    @GetMapping("/tree")
    public ResponseEntity<LocationTreeDTO> getLocationTree(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Location rootLocation = locationService.findRootLocationByUser(user).orElse(null);
        LocationTreeDTO rootTree = mapToTreeDTO(rootLocation);
        return ResponseEntity.ok(rootTree);
    }


    @PostMapping
    public ResponseEntity<?> createLocation(@RequestBody LocationCreateDTO createDTO) {
        locationService.createLocation(
                createDTO.label(),
                LocationTypeEnum.getByName(createDTO.typeId()),
                createDTO.parentId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/structure")
    public ResponseEntity<LocationStructureDTO> getLocationStructure() {
        LocationStructureDTO structure = new LocationStructureDTO();
        return ResponseEntity.ok(structure);
    }

    private LocationTreeDTO mapToTreeDTO(Location location) {
        if (location == null) {
            return null;
        }
        List<LocationTreeDTO> children = location.getChildren().stream()
                .map(this::mapToTreeDTO)
                .collect(Collectors.toList());

        return new LocationTreeDTO(
                location.getId(),
                location.getLabel(),
                location.getTypeName(),
                children
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationDetailsDTO> getLocationById(@PathVariable Long id) {
        Location location = locationService.findById(id);
        LocationStructureDTO structure = new LocationStructureDTO();

        // Remplir automatiquement les valeurs
        structure.getLocationInfo().getColumnsSchema().forEach(column -> {
            String apiField = column.getApiField();
            Object value = switch (apiField) {
                case "id" -> location.getId();
                case "label" -> location.getLabel();
                case "type" -> location.getTypeName();
                //case "address" -> location.getAddress();
                //case "city" -> location.getCity();
                default -> null;
            };
            column.setValue(value);
        });

        return ResponseEntity.ok(new LocationDetailsDTO(structure));
    }

    public record LocationDetailsDTO(LocationStructureDTO structure) {}

}