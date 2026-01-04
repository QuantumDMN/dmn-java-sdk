package com.quantumdmn.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DmnServiceTest {

    @Test
    void testCreateWithStaticToken() {
        DmnService service = new DmnService("https://api.quantumdmn.com", "test-token");
        
        assertNotNull(service.getApiClient());
        assertNotNull(service.getApi());
        assertEquals("https://api.quantumdmn.com", service.getBaseUrl());
    }

    @Test
    void testCreateWithTokenProvider() {
        int[] callCount = {0};
        DmnService service = new DmnService("https://api.quantumdmn.com", () -> {
            callCount[0]++;
            return "dynamic-token";
        });
        
        assertNotNull(service.getApiClient());
        assertNotNull(service.getApi());
    }
}
