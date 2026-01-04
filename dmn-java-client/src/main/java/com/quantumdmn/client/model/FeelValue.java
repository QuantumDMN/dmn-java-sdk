package com.quantumdmn.client.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a FEEL (Friendly Enough Expression Language) value.
 * FEEL values can be: numbers, strings, booleans, lists, contexts (maps), or null.
 * 
 * <p>This is a type-safe wrapper that preserves the semantic meaning of FEEL values
 * from the DMN engine.</p>
 */
@JsonDeserialize(using = FeelValue.Deserializer.class)
public class FeelValue {
    
    private final Object value;
    private final Type type;
    
    public enum Type {
        NUMBER,
        STRING,
        BOOLEAN,
        LIST,
        CONTEXT,
        NULL
    }
    
    private FeelValue(Object value, Type type) {
        this.value = value;
        this.type = type;
    }
    
    // factory methods
    
    public static FeelValue ofNumber(Number value) {
        return new FeelValue(value instanceof BigDecimal ? value : new BigDecimal(value.toString()), Type.NUMBER);
    }
    
    public static FeelValue ofString(String value) {
        return new FeelValue(value, Type.STRING);
    }
    
    public static FeelValue ofBoolean(boolean value) {
        return new FeelValue(value, Type.BOOLEAN);
    }
    
    public static FeelValue ofList(List<FeelValue> value) {
        return new FeelValue(value, Type.LIST);
    }
    
    public static FeelValue ofContext(Map<String, FeelValue> value) {
        return new FeelValue(value, Type.CONTEXT);
    }
    
    public static FeelValue ofNull() {
        return new FeelValue(null, Type.NULL);
    }
    
    @JsonCreator
    public static FeelValue fromRaw(Object raw) {
        if (raw == null) {
            return ofNull();
        }
        if (raw instanceof Number) {
            return ofNumber((Number) raw);
        }
        if (raw instanceof String) {
            return ofString((String) raw);
        }
        if (raw instanceof Boolean) {
            return ofBoolean((Boolean) raw);
        }
        if (raw instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) raw;
            return ofList(list.stream().map(FeelValue::fromRaw).toList());
        }
        if (raw instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) raw;
            return ofContext(map.entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                    Map.Entry::getKey,
                    e -> FeelValue.fromRaw(e.getValue())
                )));
        }
        // fallback - wrap as string
        return ofString(raw.toString());
    }
    
    // type checking
    
    public Type getType() {
        return type;
    }
    
    public boolean isNumber() { return type == Type.NUMBER; }
    public boolean isString() { return type == Type.STRING; }
    public boolean isBoolean() { return type == Type.BOOLEAN; }
    public boolean isList() { return type == Type.LIST; }
    public boolean isContext() { return type == Type.CONTEXT; }
    public boolean isNull() { return type == Type.NULL; }
    
    // typed getters
    
    public BigDecimal asNumber() {
        if (!isNumber()) throw new IllegalStateException("Not a number: " + type);
        return (BigDecimal) value;
    }
    
    public String asString() {
        if (!isString()) throw new IllegalStateException("Not a string: " + type);
        return (String) value;
    }
    
    public boolean asBoolean() {
        if (!isBoolean()) throw new IllegalStateException("Not a boolean: " + type);
        return (Boolean) value;
    }
    
    @SuppressWarnings("unchecked")
    public List<FeelValue> asList() {
        if (!isList()) throw new IllegalStateException("Not a list: " + type);
        return (List<FeelValue>) value;
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, FeelValue> asContext() {
        if (!isContext()) throw new IllegalStateException("Not a context: " + type);
        return (Map<String, FeelValue>) value;
    }
    
    @JsonValue
    public Object getRawValue() {
        if (value == null) return null;
        if (isList()) {
            return asList().stream().map(FeelValue::getRawValue).toList();
        }
        if (isContext()) {
            return asContext().entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().getRawValue()
                ));
        }
        return value;
    }
    
    /**
     * Converts this value to a URL query string format.
     * Required for compatibility with generated API client code.
     */
    public String toUrlQueryString(String prefix) {
        if (value == null) {
            return "";
        }
        return prefix + "=" + java.net.URLEncoder.encode(String.valueOf(getRawValue()), java.nio.charset.StandardCharsets.UTF_8);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeelValue feelValue = (FeelValue) o;
        return type == feelValue.type && Objects.equals(value, feelValue.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value, type);
    }
    
    @Override
    public String toString() {
        return "FeelValue{type=" + type + ", value=" + value + "}";
    }
    
    // custom deserializer
    public static class Deserializer extends JsonDeserializer<FeelValue> {
        @Override
        public FeelValue deserialize(JsonParser p, DeserializationContext ctxt) 
                throws IOException, JsonProcessingException {
            JsonNode node = p.getCodec().readTree(p);
            return deserializeNode(node);
        }
        
        private FeelValue deserializeNode(JsonNode node) {
            if (node == null || node.isNull()) {
                return FeelValue.ofNull();
            }
            if (node.isNumber()) {
                return FeelValue.ofNumber(node.decimalValue());
            }
            if (node.isTextual()) {
                return FeelValue.ofString(node.asText());
            }
            if (node.isBoolean()) {
                return FeelValue.ofBoolean(node.asBoolean());
            }
            if (node.isArray()) {
                List<FeelValue> list = new java.util.ArrayList<>();
                for (JsonNode element : node) {
                    list.add(deserializeNode(element));
                }
                return FeelValue.ofList(list);
            }
            if (node.isObject()) {
                Map<String, FeelValue> map = new java.util.LinkedHashMap<>();
                node.fields().forEachRemaining(entry -> 
                    map.put(entry.getKey(), deserializeNode(entry.getValue()))
                );
                return FeelValue.ofContext(map);
            }
            return FeelValue.ofNull();
        }
    }
}
