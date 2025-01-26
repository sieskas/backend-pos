package com.rotules.backend.domain;

public enum LocationTypeEnum {
    CHAIN(1L),
    REGION(2L),
    DISTRICT(3L),
    STORE(4L);

    private final Long id;

    LocationTypeEnum(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public static LocationTypeEnum getById(Long id) {
        for (LocationTypeEnum type : values()) {
            if (type.getId().equals(id)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No location type found with id: " + id);
    }
}