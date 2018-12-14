package com.github.kostrovik.iceguard.converters;

import com.fasterxml.jackson.core.JsonGenerator;
import com.github.kostrovik.iceguard.models.Token;

import java.io.IOException;
import java.util.Map;

/**
 * project: iceguard
 * author:  kostrovik
 * date:    2018-12-13
 * github:  https://github.com/kostrovik/iceguard
 */
public class TokenConverter extends AbstractConverter<Token> {
    @Override
    protected void prepareJson(JsonGenerator generator, Token entity) throws IOException {
        generator.writeObjectField("access_token", entity.getAccess());
        generator.writeStringField("refresh_token", entity.getRefresh());
    }

    @Override
    protected Token parseMap(Map map) {
        String access = (String) map.getOrDefault("access_token", null);
        String refresh = (String) map.getOrDefault("refresh_token", null);

        return new Token(access, refresh);
    }

    @Override
    public String toXmlString(Token entity) {
        return null;
    }

    @Override
    public String toXmlString(Map<String, String> map) {
        return null;
    }

    @Override
    public Token fromXmlString(String xml) {
        return null;
    }
}