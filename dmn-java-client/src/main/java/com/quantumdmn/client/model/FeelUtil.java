package com.quantumdmn.client.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for building FEEL contexts and lists with type-safe builders.
 * Use this to construct inputs for DMN evaluation.
 * 
 * <p>Example:</p>
 * <pre>{@code
 * Map<String, FeelValue> context = FeelUtil.contextBuilder()
 *     .put("age", 25)
 *     .put("income", 50000.0)
 *     .put("employed", true)
 *     .build();
 * }</pre>
 */
public class FeelUtil {
    
    private FeelUtil() {
        // utility class
    }
    
    /**
     * Creates a new context builder.
     */
    public static ContextBuilder contextBuilder() {
        return new ContextBuilder();
    }
    
    /**
     * Creates a new list builder.
     */
    public static ListBuilder listBuilder() {
        return new ListBuilder();
    }
    
    /**
     * Builder for creating FEEL contexts (Map\u003cString, FeelValue\u003e).
     */
    public static class ContextBuilder {
        private final Map<String, FeelValue> context = new LinkedHashMap<>();
        
        public ContextBuilder put(String key, FeelValue value) {
            context.put(key, value);
            return this;
        }
        
        public ContextBuilder put(String key, Number value) {
            context.put(key, FeelValue.ofNumber(value));
            return this;
        }
        
        public ContextBuilder put(String key, String value) {
            context.put(key, FeelValue.ofString(value));
            return this;
        }
        
        public ContextBuilder put(String key, boolean value) {
            context.put(key, FeelValue.ofBoolean(value));
            return this;
        }
        
        public ContextBuilder putNull(String key) {
            context.put(key, FeelValue.ofNull());
            return this;
        }
        
        public Map<String, FeelValue> build() {
            return context;
        }
    }
    
    /**
     * Builder for creating FEEL lists (List\u003cFeelValue\u003e).
     */
    public static class ListBuilder {
        private final java.util.List<FeelValue> list = new java.util.ArrayList<>();
        
        public ListBuilder add(FeelValue value) {
            list.add(value);
            return this;
        }
        
        public ListBuilder add(Number value) {
            list.add(FeelValue.ofNumber(value));
            return this;
        }
        
        public ListBuilder add(String value) {
            list.add(FeelValue.ofString(value));
            return this;
        }
        
        public ListBuilder add(boolean value) {
            list.add(FeelValue.ofBoolean(value));
            return this;
        }
        
        public ListBuilder addNull() {
            list.add(FeelValue.ofNull());
            return this;
        }
        
        public List<FeelValue> build() {
            return list;
        }
    }
}
