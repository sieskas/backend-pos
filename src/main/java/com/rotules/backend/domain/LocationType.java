package com.rotules.backend.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "location_types")
public class LocationType {
    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true)
    private LocationTypeEnum name;

    public LocationType() {
    }

    public LocationType(LocationTypeEnum name) {
        this.id = name.getId();
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocationTypeEnum getName() {
        return name;
    }
}