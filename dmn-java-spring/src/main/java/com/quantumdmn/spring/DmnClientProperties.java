package com.quantumdmn.spring;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for QuantumDMN client.
 * 
 * <p>Configure in application.yml:</p>
 * <pre>
 * quantumdmn:
 *   base-url: https://api.quantumdmn.com
 *   token: your-static-token  # or use token-provider bean
 * </pre>
 */
@ConfigurationProperties(prefix = "quantumdmn")
public class DmnClientProperties {
    
    /**
     * API base URL (e.g., https://api.quantumdmn.com)
     */
    private String baseUrl = "https://api.quantumdmn.com";
    
    /**
     * Static bearer token (optional, use TokenProvider bean for dynamic tokens)
     */
    private String token;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
