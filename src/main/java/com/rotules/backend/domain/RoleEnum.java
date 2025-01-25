package com.rotules.backend.domain;

public enum RoleEnum {
    ROLE_ADMIN(1L, "ROLE_ADMIN"),
    ROLE_USER(2L, "ROLE_USER");

    private final Long id;
    private final String value;

    RoleEnum(Long id, String value) {
        this.id = id;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public String getValue() {
        return value;
    }

    public static RoleEnum getById(Long id) {
        for (RoleEnum role : values()) {
            if (role.getId().equals(id)) {
                return role;
            }
        }
        throw new IllegalArgumentException("No role found with id: " + id);
    }

    public static RoleEnum getByValue(String value) {
        for (RoleEnum role : values()) {
            if (role.getValue().equals(value)) {
                return role;
            }
        }
        throw new IllegalArgumentException("No role found with value: " + value);
    }
}