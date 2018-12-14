package com.github.kostrovik.iceguard.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.github.kostrovik.iceguard.models.Permission;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * project: iceguard
 * author:  kostrovik
 * date:    2018-12-13
 * github:  https://github.com/kostrovik/iceguard
 */
public class PermissionConverter extends AbstractConverter<Permission> {
    @Override
    protected void prepareJson(JsonGenerator generator, Permission entity) throws IOException {
        generator.writeObjectField("id", entity.getId());
        generator.writeStringField("name", entity.getName());
        generator.writeStringField("code", entity.getCode());
        generator.writeStringField("description", entity.getDescription());
    }

    @Override
    protected Permission parseMap(Map map) {
        String id = (String) map.getOrDefault("id", null);
        String name = (String) map.getOrDefault("name", "");
        String code = (String) map.getOrDefault("code", null);
        String description = (String) map.getOrDefault("description", "");

        Permission permission = new Permission(UUID.fromString(id), code);
        permission.setName(name);
        permission.setDescription(description);
        return permission;
    }

    @Override
    public String toXmlString(Permission entity) {
        return null;
    }

    @Override
    public String toXmlString(Map<String, String> map) {
        return null;
    }

    @Override
    public Permission fromXmlString(String xml) {
        return null;
    }
}
