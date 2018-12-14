package com.github.kostrovik.iceguard.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * project: iceguard
 * author:  kostrovik
 * date:    2018-12-13
 * github:  https://github.com/kostrovik/iceguard
 */
public class UserRole implements Comparable<UserRole> {
    private UUID id;
    private String code;
    private String name;
    private String description;
    private List<Permission> permissions;

    public UserRole(UUID id, String code) {
        Objects.requireNonNull(id);
        this.id = id;
        Objects.requireNonNull(code);
        if (code.trim().isEmpty()) {
            throw new IllegalArgumentException("code не может быть пустой строкой");
        }
        this.code = code;

        this.name = "";
        this.description = "";
        this.permissions = new ArrayList<>();
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNullElse(name, "");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = Objects.requireNonNullElse(description, "");
    }

    public List<Permission> getPermissions() {
        return new ArrayList<>(permissions);
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = new ArrayList<>(permissions);
    }

    public void addPermission(Permission permission) {
        this.permissions.add(permission);
    }

    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    @Override
    public int compareTo(UserRole o) {
        String selfName = getName();
        String comparedName = o.getName();

        if (!selfName.trim().isEmpty() && !comparedName.trim().isEmpty()) {
            return selfName.compareTo(comparedName);
        }

        return getCode().compareTo(o.getCode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRole)) return false;
        UserRole userRole = (UserRole) o;
        return id.equals(userRole.id) &&
                code.equals(userRole.code) &&
                Objects.equals(name, userRole.name) &&
                Objects.equals(description, userRole.description) &&
                Objects.equals(permissions, userRole.permissions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, name, description, permissions);
    }
}
