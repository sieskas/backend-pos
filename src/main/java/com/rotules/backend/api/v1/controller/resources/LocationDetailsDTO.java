package com.rotules.backend.api.v1.controller.resources;

public record LocationDetailsDTO(
        Long id,
        String label,
        String type,
        LocationStructureDTO structure
) {}