package com.github.kostrovik.iceguard.converters;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kostrovik.iceguard.exceptions.ConverterException;
import com.github.kostrovik.useful.interfaces.ConverterInterface;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.Objects;

/**
 * project: iceguard
 * author:  kostrovik
 * date:    2018-12-13
 * github:  https://github.com/kostrovik/iceguard
 */
public abstract class AbstractConverter<E> implements ConverterInterface<E> {
    private JsonFactory jsonFactory;
    private ObjectMapper mapper;

    protected AbstractConverter() {
        this.jsonFactory = new JsonFactory();
        this.mapper = new ObjectMapper();
    }

    @Override
    public String toJsonString(E entity) {
        StringWriter writer = new StringWriter();

        if (Objects.nonNull(entity)) {
            try (JsonGenerator jsonGenerator = jsonFactory.createGenerator(writer)) {
                jsonGenerator.setCodec(mapper);
                jsonGenerator.writeStartObject();
                prepareJson(jsonGenerator, entity);
                jsonGenerator.writeEndObject();
            } catch (IOException error) {
                throw new ConverterException(error);
            }
        }

        return writer.toString();
    }

    @Override
    public String toJsonString(Map<String, String> map) {
        if (Objects.isNull(map) || map.isEmpty()) {
            return "";
        }

        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map);
        } catch (IOException error) {
            throw new ConverterException(error);
        }
    }

    @Override
    public E fromJsonString(String json) {
        try {
            Map parsedData = mapper.readValue(json, Map.class);
            return fromMap(parsedData);
        } catch (IOException e) {
            throw new ConverterException(e);
        }
    }

    @Override
    public E fromMap(Map map) {
        return parseMap(map);
    }

    protected abstract void prepareJson(JsonGenerator generator, E entity) throws IOException;

    protected abstract E parseMap(Map map);
}