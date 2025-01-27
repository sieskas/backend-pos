package com.rotules.backend.api.v1.controller.resources;

import java.util.List;

public record LocationTreeDTO(Long id, String label, String type, List<LocationTreeDTO> children) {
}
