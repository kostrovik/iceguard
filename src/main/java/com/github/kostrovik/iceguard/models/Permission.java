package com.github.kostrovik.iceguard.models;

import java.util.Objects;
import java.util.UUID;

/**
 * project: iceguard
 * author:  kostrovik
 * date:    2018-12-13
 * github:  https://github.com/kostrovik/iceguard
 */
public class Permission implements Comparable<Permission> {
    private UUID id;
    private String name;
    private String code;
    private String description;

    public Permission(UUID id, String code) {
        Objects.requireNonNull(id);
        this.id = id;
        Objects.requireNonNull(code);
        if (code.trim().isEmpty()) {
            throw new IllegalArgumentException("code не может быть пустой строкой");
        }
        this.code = code;

        this.name = "";
        this.description = "";
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = Objects.requireNonNullElse(name, "");
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = Objects.requireNonNullElse(description, "");
    }

    @Override
    public int compareTo(Permission o) {
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
        if (!(o instanceof Permission)) return false;
        Permission that = (Permission) o;
        return id.equals(that.id) &&
                Objects.equals(name, that.name) &&
                code.equals(that.code) &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, code, description);
    }
}
