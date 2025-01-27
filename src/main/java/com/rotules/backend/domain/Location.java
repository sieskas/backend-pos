package com.rotules.backend.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "locations")
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String label;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_id", nullable = false)
    private LocationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Location parent;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    @OrderBy("label ASC")
    private Set<Location> children = new HashSet<>();

    @OneToMany(mappedBy = "rootLocation", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<User> users = new HashSet<>();

    // Méthodes pour la gestion de la hiérarchie
    public void addChild(Location child) {
        children.add(child);
        child.setParent(this);
    }

    public void removeChild(Location child) {
        children.remove(child);
        child.setParent(null);
    }

    // Méthode récursive pour obtenir tous les enfants (inclut les enfants des enfants)
    @Transient
    public List<Location> getAllChildren() {
        List<Location> allChildren = new ArrayList<>();
        for (Location child : children) {
            allChildren.add(child);
            allChildren.addAll(child.getAllChildren());
        }
        return allChildren;
    }

    // Méthode pour obtenir le chemin complet depuis la racine
    @Transient
    public List<Location> getPath() {
        List<Location> path = new ArrayList<>();
        Location current = this;
        while (current != null) {
            path.add(0, current);
            current = current.getParent();
        }
        return path;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public LocationType getType() {
        return type;
    }

    public String getTypeName() {
        return type.getName().name();
    }

    public void setType(LocationTypeEnum typeEnum) {
        this.type = new LocationType(typeEnum);
    }

    public Location getParent() {
        return parent;
    }

    public void setParent(Location parent) {
        this.parent = parent;
    }

    public Set<Location> getChildren() {
        return children;
    }

    public void setChildren(Set<Location> children) {
        this.children = children;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void addUser(User user) {
        if (!users.contains(user)) {
            users.add(user);
            if (user.getRootLocation() != this) {
                user.setRootLocation(this);
            }
        }
    }

    public void removeUser(User user) {
        if (users.contains(user)) {
            users.remove(user);
            if (user.getRootLocation() == this) {
                user.setRootLocation(null);
            }
        }
    }
}
