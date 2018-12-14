package com.github.kostrovik.iceguard.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.github.kostrovik.iceguard.models.Permission;
import com.github.kostrovik.iceguard.models.UserRole;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * project: iceguard
 * author:  kostrovik
 * date:    2018-12-13
 * github:  https://github.com/kostrovik/iceguard
 */
public class UserRoleConverter extends AbstractConverter<UserRole> {
    private PermissionConverter permissionConverter;

    public UserRoleConverter() {
        this.permissionConverter = new PermissionConverter();
    }

    @Override
    protected void prepareJson(JsonGenerator generator, UserRole entity) throws IOException {
        generator.writeObjectField("id", entity.getId());
        generator.writeStringField("code", entity.getCode());
        generator.writeStringField("name", entity.getName());
        generator.writeStringField("description", entity.getDescription());
        generator.writeArrayFieldStart("permissions");
        for (Permission permission : entity.getPermissions()) {
            generator.writeString(permissionConverter.toJsonString(permission));
        }
        generator.writeEndArray();
    }

    @Override
    protected UserRole parseMap(Map map) {
        String id = (String) map.getOrDefault("id", null);
        String name = (String) map.getOrDefault("name", "");
        String code = (String) map.getOrDefault("code", null);
        String description = (String) map.getOrDefault("description", "");
        List<Map> permissionsMap = (List<Map>) map.getOrDefault("permissions", new ArrayList<>());

        List<Permission> permissions = new ArrayList<>();
        for (Map permissionMap : permissionsMap) {
            permissions.add(permissionConverter.parseMap(permissionMap));
        }

        UserRole role = new UserRole(UUID.fromString(id), code);
        role.setName(name);
        role.setDescription(description);
        role.setPermissions(permissions);

        return role;
    }

    @Override
    public String toXmlString(UserRole entity) {
        return null;
    }

    @Override
    public String toXmlString(Map<String, String> map) {
        return null;
    }

    @Override
    public UserRole fromXmlString(String xml) {
        return null;
    }
}