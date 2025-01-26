package com.rotules.backend.api.v1.controller.resources.auth;

import java.util.List;

public record UserDTO(String username, List<String> roles, PersonDTO person) {
}
