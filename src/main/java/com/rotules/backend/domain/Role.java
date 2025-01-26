package com.rotules.backend.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "roles")
public class Role {
    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true)
    private RoleEnum name;

    public Role() {}

    public Role(RoleEnum name) {
        this.id = name.getId();
        this.name = name;
    }

    public String getAuthority() {
        return name.name();
    }

    // Getters et setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RoleEnum getName() {
        return name;
    }

    public void setName(RoleEnum name) {
        this.id = name.getId();
        this.name = name;
    }
}