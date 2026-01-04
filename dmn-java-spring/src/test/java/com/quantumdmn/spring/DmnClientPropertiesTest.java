package com.quantumdmn.spring;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DmnClientPropertiesTest {

    @Test
    void testDefaultBaseUrl() {
        DmnClientProperties props = new DmnClientProperties();
        assertEquals("https://api.quantumdmn.com", props.getBaseUrl());
    }

    @Test
    void testSetBaseUrl() {
        DmnClientProperties props = new DmnClientProperties();
        props.setBaseUrl("https://custom.api.com");
        assertEquals("https://custom.api.com", props.getBaseUrl());
    }

    @Test
    void testSetToken() {
        DmnClientProperties props = new DmnClientProperties();
        assertNull(props.getToken());
        props.setToken("test-token");
        assertEquals("test-token", props.getToken());
    }
}
