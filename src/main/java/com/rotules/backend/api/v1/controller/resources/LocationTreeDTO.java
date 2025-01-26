package com.rotules.backend.api.v1.controller.resources;

import java.util.List;

public record LocationTreeDTO(Long id, String name, List<LocationTreeDTO> children) {
}