package com.rotules.backend.domain;

public enum RoleEnum {
    ROLE_ADMIN(1L),
    ROLE_USER(2L);

    private final Long id;

    RoleEnum(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public static RoleEnum getById(Long id) {
        for (RoleEnum role : values()) {
            if (role.getId().equals(id)) {
                return role;
            }
        }
        throw new IllegalArgumentException("No role found with id: " + id);
    }
}